/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import com.mysema.query.QueryException
import com.mysema.query.sql.RelationalPath
import com.mysema.query.sql.SQLQuery
import com.mysema.query.sql.dml.SQLInsertClause
import com.mysema.query.sql.dml.SQLUpdateClause
import java.nio.ByteBuffer
import java.sql.Connection
import java.util.UUID

inline fun <T : AutoCloseable, R> T.use(block: T.() -> R): R {
    var closed = false
    try {
        return block()
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

/**
 * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
 * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
 * caller.
 *
 * @param insertClause The action to run for the insert query
 * @param updateClause The action to run for the update query if the insert fails
 * @param plugin The StatCraft object
 */
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

/**
 * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
 * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
 * caller. This also allows work to be done before the insert and update queries. The work function can return any
 * object, and this object will be passed to the insert and update functions, and in that order. Because of this, if
 * the object is modified in the insert function, these modifications will be present in the update function.
 *
 * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
 * @param insertClause The action to run for the insert query
 * @param updateClause The action to run for the update query if the insert fails
 * @param plugin The StatCraft object
 */
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

/**
 * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
 * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
 * caller.
 *
 * For convenience this method also allows a player's UUID and world UUID to be passed in. The database id of the
 * player and world will be fetched before the insert and update functions are called, and the id will be passed to
 * them. This is not an expensive operation as both of these values are cached.
 *
 * Thanks to type inferencing no type parameters should need to be explicitly provided.
 *
 * @param playerId The UUID of the relevant player
 * @param worldName The UUID of the relevant world
 * @param insertClause The action to run for the insert query
 * @param updateClause The action to run for the update query if the insert fails
 * @param plugin The StatCraft object
 */
inline fun <T : RelationalPath<*>> T.runQuery(playerId: UUID,
                                              worldName: String,
                                              insertClause: (T, SQLInsertClause, Int, Int) -> Unit,
                                              updateClause: (T, SQLUpdateClause, Int, Int) -> Unit,
                                              connection: Connection,
                                              plugin: StatCraft) {

    val id = plugin.databaseManager.getPlayerId(playerId) ?: return
    val wid = plugin.databaseManager.getWorldId(worldName) ?: return

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause, id, wid)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause, id, wid)
    }
}

/**
 * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
 * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
 * caller. This also allows work to be done before the insert and update queries. The work function can return any
 * object, and this object will be passed to the insert and update functions, and in that order. Because of this, if
 * the object is modified in the insert function, these modifications will be present in the update function.
 *
 * For convenience this method also allows a player's UUID and a world's UUID to be passed in. The database id of
 * the player and world will be fetched before the insert and update functions are called, and the id will be
 * passed to them. This is not an expensive operation as both of these values are cached.
 *
 * Thanks to type inferencing no type parameters should need to be explicitly provided.
 *
 * @param playerId The UUID of the relevant player
 * @param worldName The UUID of the relevant world
 * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
 * @param insertClause The action to run for the insert query
 * @param updateClause The action to run for the update query if the insert fails
 * @param plugin The StatCraft object
 */
inline fun <T : RelationalPath<*>, R> T.runQuery(playerId: UUID,
                                                 worldName: String,
                                                 workBefore: (T, SQLQuery, Int, Int) -> R,
                                                 insertClause: (T, SQLInsertClause, Int, Int, R) -> Unit,
                                                 updateClause: (T, SQLUpdateClause, Int, Int, R) -> Unit,
                                                 connection: Connection,
                                                 plugin: StatCraft) {

    val id = plugin.databaseManager.getPlayerId(playerId) ?: return
    val wid = plugin.databaseManager.getWorldId(worldName) ?: return

    val r = workBefore(this, plugin.databaseManager.getNewQuery(connection) ?: return, id, wid)

    try {
        val clause = plugin.databaseManager.getInsertClause(connection, this) ?: return

        insertClause(this, clause, id, wid, r)
    } catch (e: QueryException) {
        val clause = plugin.databaseManager.getUpdateClause(connection, this) ?: return

        updateClause(this, clause, id, wid, r)
    }
}
