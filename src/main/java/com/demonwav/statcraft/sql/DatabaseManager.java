/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Table;
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.mysema.query.sql.MySQLTemplates;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.OfflinePlayer;
import org.mariadb.jdbc.MariaDbDataSource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager implements Closeable {

    private StatCraft plugin;
    private boolean connecting = true;
    private HikariDataSource dataSource;

    public DatabaseManager(StatCraft plugin) {
        this.plugin = plugin;
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(MariaDbDataSource.class.getName());
        config.setUsername(plugin.config().getMysql().getUsername());
        config.setPassword(plugin.config().getMysql().getPassword());
        config.addDataSourceProperty("databaseName", plugin.config().getMysql().getDatabase());
        config.addDataSourceProperty("portNumber", plugin.config().getMysql().getPort());
        config.addDataSourceProperty("serverName", plugin.config().getMysql().getHostname());
        config.setMaximumPoolSize(Table.values().length);

        dataSource = new HikariDataSource(config);

        try (Connection ignored = dataSource.getConnection()) {
            connecting = false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            plugin.getLogger().severe(red(" *** StatCraft was unable to communicate with the database,"));
            plugin.getLogger().severe(red(" *** please check your settings and reload, StatCraft will"));
            plugin.getLogger().severe(red(" *** now be disabled."));
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void setupDatabase()  {
        for (Table table : Table.values()) {
            checkTable(table);
            if (!plugin.isEnabled())
                break;
        }
        if (plugin.isEnabled())
            plugin.getLogger().info("Database verified successfully.");
    }

    @SuppressWarnings("SqlResolve")
    private void checkTable(Table table) {
        PreparedStatement intPst = null;
        ResultSet resultSet = null;
        ResultSet intResultSet = null;
        try (
            final Connection connection = getConnection();
            final PreparedStatement pst = connection.prepareStatement("SELECT ENGINE FROM information_schema.TABLES where TABLE_NAME = ?;")
        ) {
            pst.setString(1, table.getName());
            try {
                // This can fail because there is no table
                resultSet = pst.executeQuery();
            } catch (SQLException e) {
                remakeTable(table, false);
            }

            if (resultSet == null) {
                remakeTable(table, false);
                return;
            }

            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, table.getName(), null);
            if (tables.next() && resultSet.next()) {
                // Table exists
                // Make sure the engine is correct
                if (!resultSet.getString("ENGINE").equals("InnoDB")) {
                    plugin.getLogger().warning(table.getName() + " is using an incorrect engine.");
                    remakeTable(table, true);
                } else {
                    // Make sure the columns are correct
                    intPst = connection.prepareStatement(String.format("SHOW COLUMNS FROM %s;", StringEscapeUtils.escapeSql(table.getName())));
                    intResultSet = intPst.executeQuery();

                    // Make sure there are columns
                    if (intResultSet.last()) {
                        // Make sure there is the correct number of columns
                        if (intResultSet.getRow() != table.getColumns()) {
                            plugin.getLogger().warning(table.getName() + " has an incorrect number of columns.");
                            remakeTable(table, true);
                        } else {
                            // Make sure the columns are correct
                            intResultSet.beforeFirst();
                            for (String column : table.getColumnNames()) {
                                intResultSet.next();
                                if (!intResultSet.getString("Field").equals(column)) {
                                    plugin.getLogger().warning(table.getName() + " has incorrect columns.");
                                    remakeTable(table, true);
                                    break;
                                }
                            }
                        }
                    } else {
                        plugin.getLogger().warning(table.getName() + " has no columns");
                        remakeTable(table, true);
                    }
                }
            } else {
                // Table does not exist
               remakeTable(table, false);
            }
            tables.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resultSet
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Close intPst
            try {
                if (intPst != null)
                    intPst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Close intResultSet
            try {
                if (intResultSet != null)
                    intResultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void remakeTable(Table table, boolean ask) {
        if (!ask) {
            dropTable(table);
            createTable(table);
            plugin.getLogger().info("Created table `" + table.getName() + "`.");
        } else {
            if (plugin.config().getMysql().isForceSetup()) {
                dropTable(table);
                createTable(table);
                plugin.getLogger().info("Created table `" + table.getName() + "`.");
            } else {
                plugin.getLogger().severe(
                    red("*** " + table.getName() + " is not set up correctly and conflicts with StatCraft's setup."));
                plugin.getLogger().severe(
                    red("*** Change mysql.forceSetup, remove or rename this table, or create a new database for"));
                plugin.getLogger().severe(
                    red("*** StatCraft to use. StatCraft will not run unless there are no conflicting tables."));
                plugin.getLogger().severe(
                    red("*** StatCraft will now be disabled."));
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
    }

    private String red(String text) {
        return "\u001b[1;31m" + text + "\u001b[m";
    }

    private void dropTable(Table table) {
        try (
            final Connection connection = getConnection();
            final PreparedStatement pst = connection.prepareStatement(
                String.format("DROP TABLE IF EXISTS %s;", StringEscapeUtils.escapeSql(table.getName())))
        ) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(Table table) {
        try (
            final Connection connection = getConnection();
            final PreparedStatement pst = connection.prepareStatement(table.getCreate())
        ) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public int getPlayerId(UUID uuid) {
        byte[] array = Util.UUIDToByte(uuid);
        try (Connection connection = getConnection()) {
            SQLQuery query = new SQLQuery(connection, SQLTemplates.DEFAULT);
            Integer res = query
                .from(QPlayers.players)
                .where(QPlayers.players.uuid.eq(array))
                .uniqueResult(QPlayers.players.id);

            return res == null ? -1 : res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getPlayerId(String name) {
        try (final Connection connection = getConnection()) {
            SQLQuery query = getNewQuery(connection);
            QPlayers p = QPlayers.players;
            Integer res = query
                .from(p)
                .where(p.name.eq(name))
                .uniqueResult(p.id);

            if (res == null) {
                // it failed to find a player by that name, so attempt to do a UUID lookup
                @SuppressWarnings("deprecation")
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);

                // Check if it's an offline UUID
                if (player.getUniqueId().version() < 4)
                    return -1;

                res = query
                    .from(p)
                    .where(p.uuid.eq(Util.UUIDToByte(player.getUniqueId())))
                    .uniqueResult(p.id);

                if (res == null)
                    return -1;

                // fix the UUID / name pairing
                synchronized (this) {
                    SQLUpdateClause clause = new SQLUpdateClause(connection, SQLTemplates.DEFAULT, p);
                    clause
                        .where(p.uuid.eq(Util.UUIDToByte(player.getUniqueId())))
                        .set(p.name, name)
                        .execute();
                }

                return res;
            } else {
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public SQLQuery getNewQuery(Connection connection) {
        if (!connecting) {
            return new SQLQuery(connection, MySQLTemplates.DEFAULT);
        } else {
            return null;
        }
    }

    public SQLUpdateClause getUpdateClause(Connection connection, RelationalPath<?> path) {
        if (!connecting) {
            return new SQLUpdateClause(connection, MySQLTemplates.DEFAULT, path);
        } else {
            return null;
        }
    }

    public SQLInsertClause getInsertClause(Connection connection, RelationalPath<?> path) {
        if (!connecting) {
            return new SQLInsertClause(connection, MySQLTemplates.DEFAULT, path);
        } else {
            return null;
        }
    }
}
