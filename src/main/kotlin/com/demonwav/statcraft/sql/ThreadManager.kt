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
import com.demonwav.statcraft.iter
import com.demonwav.statcraft.runQuery
import com.demonwav.statcraft.use
import com.mysema.query.sql.RelationalPath
import com.mysema.query.sql.SQLQuery
import com.mysema.query.sql.dml.SQLInsertClause
import com.mysema.query.sql.dml.SQLUpdateClause
import org.bukkit.Bukkit
import java.io.Closeable
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ThreadManager(val plugin: StatCraft) : Runnable, Closeable {

    val map = ConcurrentHashMap<Class<*>, ConcurrentLinkedQueue<(Connection) -> Unit>>()
    val work = ConcurrentHashMap<Class<*>, Int>()

    override fun run() {
        work.entries.retainAll { Bukkit.getScheduler().isCurrentlyRunning(it.value) }

        map.entries.iter {
            if (!work.contains(it.key)) {
                work.put(
                    it.key,
                    plugin.server.scheduler.runTaskAsynchronously(plugin,
                        WorkerInstance(it.value, plugin)
                    ).taskId
                )
                remove()
            }
        }
    }

    // java style, until everything is converted to kotlin
    fun <T : RelationalPath<*>> schedule(clazz: Class<T>,
                                         playerId: UUID,
                                         worldName: String,
                                         insertRunner: QueryIdRunner<T, SQLInsertClause>,
                                         updateRunner: QueryIdRunner<T, SQLUpdateClause>) {

        scheduleRaw(clazz) { connection ->
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)
            path.runQuery(playerId, worldName, { t, sqlInsertClause, id, worldId ->
                insertRunner.run(t, sqlInsertClause, id, worldId)
            }, { t, sqlUpdateClause, id, worldId ->
                updateRunner.run(t, sqlUpdateClause, id, worldId)
            }, connection, plugin)
        }
    }

    fun <T : RelationalPath<*>, R> schedule(clazz: Class<T>,
                                            playerId: UUID,
                                            worldName: String,
                                            workBefore: (T, SQLQuery, Int, Int) -> R,
                                            insertRunner: QueryIdRunnerMap<T, SQLInsertClause, R>,
                                            updateRunner: QueryIdRunnerMap<T, SQLUpdateClause, R>) {

        scheduleRaw(clazz) { connection ->
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)
            path.runQuery(playerId, worldName, workBefore,
            { t, sqlInsertClause, id, worldId, r ->
                insertRunner.run(t, sqlInsertClause, id, worldId, r)
            }, { t, sqlUpdateClause, id, worldId, r ->
                updateRunner.run(t, sqlUpdateClause, id, worldId, r)
            }, connection, plugin)
        }
    }

    // kotlin style
    inline fun <reified T : RelationalPath<*>> schedule(playerId: UUID,
                                                        worldName: String,
                                                        /* TODO crossinline */ noinline insertRunner: (T, SQLInsertClause, Int, Int) -> Unit,
                                                        /* TODO crossinline */ noinline updateRunner: (T, SQLUpdateClause, Int, Int) -> Unit) {
        scheduleRaw(T::class.java) { connection ->
            val path = T::class.java.getConstructor(String::class.java).newInstance(T::class.java.simpleName)
            path.runQuery(playerId, worldName, insertRunner, updateRunner, connection, plugin)
        }
    }

    inline fun <reified T : RelationalPath<*>, R> schedule(playerId: UUID,
                                                           worldName: String,
                                                           /* TODO crossinline */ noinline workBefore: (T, SQLQuery, Int, Int) -> R,
                                                           /* TODO crossinline */ noinline insertRunner: (T, SQLInsertClause, Int, Int, R) -> Unit,
                                                           /* TODO crossinline */ noinline updateRunner: (T, SQLUpdateClause, Int, Int, R) -> Unit) {

        scheduleRaw(T::class.java) { connection ->
            val path = T::class.java.getConstructor(String::class.java).newInstance(T::class.java.simpleName)
            path.runQuery(playerId, worldName, workBefore, insertRunner, updateRunner, connection, plugin)
        }
    }

    fun scheduleRaw(clazz: Class<*>, consumer: (Connection) -> Unit) {
        var queue = map[clazz]
        if (queue == null) {
            queue = ConcurrentLinkedQueue()
            map[clazz] = queue
        }
        queue.offer(consumer)
    }

    override fun close() {
        work.clear()
        map.forEach { key, queue ->
            plugin.databaseManager.connection.use {
                var consumer = queue.poll()
                while (consumer != null) {
                    try {
                        consumer(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    consumer = queue.poll()
                }
            }
        }
        map.clear()
    }
}
