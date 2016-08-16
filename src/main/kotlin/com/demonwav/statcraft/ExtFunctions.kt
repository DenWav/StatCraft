/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import com.mysema.query.Query
import com.mysema.query.QueryException
import com.mysema.query.sql.RelationalPath
import com.mysema.query.sql.SQLQuery
import com.mysema.query.sql.dml.SQLInsertClause
import com.mysema.query.sql.dml.SQLUpdateClause
import java.nio.ByteBuffer
import java.sql.Connection
import java.util.UUID

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {}
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

fun UUID.toByte(): ByteArray {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16))
    byteBuffer.putLong(mostSignificantBits)
    byteBuffer.putLong(leastSignificantBits)
    return byteBuffer.array()
}

fun ByteArray.toUUID(): UUID {
    val buffer = ByteBuffer.wrap(this)
    return UUID(buffer.long, buffer.long)
}

inline fun <T> MutableIterable<T>.iter(func: MutableIterator<T>.(T) -> Unit) {
    val iter = iterator()

    while (iter.hasNext()) {
        val item = iter.next()
        iter.func(item)
    }
}

inline fun <T : RelationalPath<*>> T.runQuery(insertClause: (T, SQLInsertClause) -> Unit,
                                              updateClause: (T, SQLUpdateClause) -> Unit,
                                              connection: Connection,
                                              plugin: StatCraft) {

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause)
    }
}

inline fun <T : RelationalPath<*>, R> T.runQuery(workBefore: (T, SQLQuery) -> R,
                                                 insertClause: (T, SQLInsertClause, R) -> Unit,
                                                 updateClause: (T, SQLUpdateClause, R) -> Unit,
                                                 connection: Connection,
                                                 plugin: StatCraft) {

    val r = workBefore(this, plugin.databaseManager.getNewQuery(connection) ?: return)

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause, r)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause, r)
    }
}

inline fun <T : RelationalPath<*>> T.runQuery(playerId: UUID,
                                              worldName: String,
                                              insertClause: (T, SQLInsertClause, Int, Int) -> Unit,
                                              updateClause: (T, SQLUpdateClause, Int, Int) -> Unit,
                                              connection: Connection,
                                              plugin: StatCraft) {

    val id = plugin.databaseManager.getPlayerId(playerId)
    val wid = plugin.databaseManager.getWorldId(worldName)

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause, id, wid)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause, id, wid)
    }
}

inline fun <T : RelationalPath<*>, R> T.runQuery(playerId: UUID,
                                                 worldName: String,
                                                 workBefore: (T, SQLQuery, Int, Int) -> R,
                                                 insertClause: (T, SQLInsertClause, Int, Int, R) -> Unit,
                                                 updateClause: (T, SQLUpdateClause, Int, Int, R) -> Unit,
                                                 connection: Connection,
                                                 plugin: StatCraft) {

    val id = plugin.databaseManager.getPlayerId(playerId)
    val wid = plugin.databaseManager.getWorldId(worldName)

    val r = workBefore(this, plugin.databaseManager.getNewQuery(connection) ?: return, id, wid)

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause, id, wid, r)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause, id, wid, r)
    }
}
