/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.Table
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QWorlds
import com.demonwav.statcraft.toByte
import com.demonwav.statcraft.use
import com.mysema.query.sql.MySQLTemplates
import com.mysema.query.sql.RelationalPath
import com.mysema.query.sql.SQLQuery
import com.mysema.query.sql.dml.SQLInsertClause
import com.mysema.query.sql.dml.SQLUpdateClause
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang.StringEscapeUtils
import org.mariadb.jdbc.MariaDbDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DatabaseManager(private val plugin: StatCraft) : AutoCloseable {

    private var connecting: Boolean = true
    private lateinit var dataSource: HikariDataSource

    private val uuidMap = ConcurrentHashMap<UUID, Int>()
    private val worldMap = ConcurrentHashMap<String, Int>()

    val connection: Connection
        get() {
            return dataSource.connection
        }

    init {
        val config = HikariConfig()
        config.dataSourceClassName = MariaDbDataSource::class.java.name
        config.username = plugin.config.mysql.username
        config.password = plugin.config.mysql.password
        config.addDataSourceProperty("databaseName", plugin.config.mysql.database)
        config.addDataSourceProperty("portNumber", plugin.config.mysql.port)
        config.addDataSourceProperty("serverName", plugin.config.mysql.hostname)
        config.maximumPoolSize = Table.values().size

        try {
            dataSource = HikariDataSource(config)
            connecting = false
        } catch (e: Exception) {
            e.printStackTrace()

            plugin.logger.severe(red(" *** StatCraft was unable to communicate with the database,"))
            plugin.logger.severe(red(" *** please check your settings and reload, StatCraft will"))
            plugin.logger.severe(red(" *** now be disabled."))
            plugin.pluginLoader.disablePlugin(plugin)
        }
    }

    fun setupDatabase() {
        Table.values().forEach { table ->
            checkTable(table)
            if (!plugin.isEnabled) {
                return@forEach
            }
        }
        if (plugin.isEnabled) {
            plugin.logger.info("Database verified successfully.")
        }
    }

    private fun checkTable(table: Table) {
        var intPst: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var intResultSet: ResultSet? = null

        try {
            connection.use {
                val conn = this
                prepareStatement("SELECT ENGINE FROM information_schema.TABLES where TABLE_NAME = ?;").use {
                    setString(1, table.getName())
                    try {
                        // This can fail because there is no table
                        resultSet = executeQuery()
                    } catch (e: SQLException) {
                        remakeTable(table, false)
                        return
                    }

                    if (resultSet == null) {
                        remakeTable(table, false)
                        return
                    }

                    val dbm = conn.metaData
                    val tables = dbm.getTables(null, null, table.getName(), null)

                    tables.use {
                        if (tables.next() && resultSet!!.next()) {
                            // Table exists
                            // Make sure the engine is correct
                            if (resultSet!!.getString("ENGINE") != "InnoDB") {
                                plugin.logger.warning("${table.getName()} is using an incorrect engine.")
                                remakeTable(table, true)
                            } else {
                                // Make sure the columns are correct
                                intPst = conn.prepareStatement("SHOW COLUMNS FROM ${StringEscapeUtils.escapeSql(table.getName())};")
                                intResultSet = intPst?.executeQuery()

                                // Make sure there are columns
                                if (intResultSet?.last() == true) {
                                    // Make sure there is the correct number of columns
                                    if (intResultSet?.row != table.columns) {
                                        plugin.logger.warning("${table.getName()} has an incorrect number of columns.")
                                        remakeTable(table, true)
                                    } else {
                                        // Make sure the columns are correct
                                        intResultSet?.beforeFirst();
                                        table.columnNames.forEach { column ->
                                            intResultSet?.next()
                                            if (intResultSet?.getString("Field") != column) {
                                                plugin.logger.warning("${table.getName()} has incorrect columns.")
                                                remakeTable(table, true)
                                                return@forEach
                                            }
                                        }
                                    }
                                } else {
                                    plugin.logger.warning("${table.getName()} has no columns.")
                                    remakeTable(table, true)
                                }
                            }
                        } else {
                            // Table does not exist
                            remakeTable(table, false)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try { resultSet?.close() } catch (e: Exception) { e.printStackTrace() }
            try { intPst?.close() } catch (e: Exception) { e.printStackTrace() }
            try { intResultSet?.close() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun remakeTable(table: Table, ask: Boolean) {
        if (!ask) {
            dropTable(table)
            createTable(table)
            plugin.logger.info("Created table `${table.getName()}`")
        } else {
            if (plugin.config.mysql.forceSetup) {
                dropTable(table)
                createTable(table)
                plugin.logger.info("Created table `${table.getName()}`")
            } else {
                plugin.logger.severe(red(" *** ${table.getName()} is not setup correctly and conflicts with StatCraft's setup."))
                plugin.logger.severe(red(" *** Change mysql.forceSetup, remove or rename this table, or create a new database for"))
                plugin.logger.severe(red(" *** StatCraft to use. StatCraft will not run unless there are no conflicting tables."))
                plugin.logger.severe(red(" *** StatCraft will now be disabled."))
                plugin.pluginLoader.disablePlugin(plugin)
            }
        }
    }

    private fun red(text: String) = "\u001b[1;31m$text\u001b[m"

    private fun dropTable(table: Table) {
        try {
            connection.use {
                prepareStatement("DROP TABLE IF EXISTS ${StringEscapeUtils.escapeSql(table.getName())};").use {
                    executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun createTable(table: Table) {
        try {
            connection.use {
                prepareStatement(table.create).use {
                    executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun close() {
        dataSource.close()
    }

    fun getPlayerId(uuid: UUID): Int? {
        if (uuidMap.contains(uuid)) {
            return uuidMap[uuid]!!
        }

        val array = uuid.toByte()
        try {
            connection.use {
                val query = getNewQuery(this) ?: return null

                val res = query
                    .from(QPlayers.players)
                    .where(QPlayers.players.uuid.eq(array))
                    .uniqueResult(QPlayers.players.id)

                if (res != null) {
                    uuidMap[uuid] = res
                }

                return res
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    fun getPlayerId(name: String): Int? {
        val lock = this
        try {
            connection.use {
                val query = getNewQuery(this) ?: return null
                val p = QPlayers.players

                var res = query
                    .from(p)
                    .where(p.name.eq(name))
                    .uniqueResult(p.id)

                if (res == null) {
                    // It failed to find a player by that name, so attempt to d a UUID lookup
                    @Suppress("DEPRECATION")
                    val player = plugin.server.getOfflinePlayer(name)

                    if (player.uniqueId.version() < 4) {
                        return null
                    }

                    res = query
                        .from(p)
                        .where(p.uuid.eq(player.uniqueId.toByte()))
                        .uniqueResult(p.id) ?: return null

                    synchronized(lock) {
                        val clause = getUpdateClause(connection, p)

                        clause
                            ?.where(p.uuid.eq(player.uniqueId.toByte()))
                            ?.set(p.name, name)
                            ?.execute()
                    }

                    uuidMap[player.uniqueId] = res

                    return res
                } else {
                    return res
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    fun getWorldId(worldName: String): Int? {
        if (worldMap.contains(worldName)) {
            return worldMap[worldName]
        }

        try {
            connection.use {
                var query = getNewQuery(this) ?: return null
                val w = QWorlds.worlds

                var res = query.from(w).where(w.worldName.eq(worldName)).uniqueResult(w.worldId)
                if (res != null) {
                    worldMap[worldName] = res
                    return res
                }

                val clause = getInsertClause(this, w) ?: return null

                clause.columns(w.worldName, w.customName).values(worldName, worldName).execute()

                query = getNewQuery(this) ?: return null

                res = query.from(w).where(w.worldName.eq(worldName)).uniqueResult(w.worldId)
                if (res != null) {
                    worldMap[worldName] = res
                }

                return res
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    fun getNewQuery(connection: Connection): SQLQuery? {
        if (!connecting) {
            return SQLQuery(connection, MySQLTemplates.DEFAULT)
        } else {
            return null
        }
    }

    fun getUpdateClause(connection: Connection, path: RelationalPath<*>): SQLUpdateClause? {
        if (!connecting) {
            return SQLUpdateClause(connection, MySQLTemplates.DEFAULT, path)
        } else {
            return null
        }
    }

    fun getInsertClause(connection: Connection, path: RelationalPath<*>): SQLInsertClause? {
        if (!connecting) {
            return SQLInsertClause(connection, MySQLTemplates.DEFAULT, path)
        } else {
            return null
        }
    }
}
