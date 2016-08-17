/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

@file:JvmName("Util")
package com.demonwav.statcraft

import com.demonwav.statcraft.sql.QueryFunction
import com.demonwav.statcraft.sql.QueryIdFunction
import com.demonwav.statcraft.sql.QueryIdRunner
import com.demonwav.statcraft.sql.QueryIdRunnerMap
import com.demonwav.statcraft.sql.QueryRunner
import com.demonwav.statcraft.sql.QueryRunnerMap
import com.mysema.query.QueryException
import com.mysema.query.sql.RelationalPath
import com.mysema.query.sql.dml.SQLInsertClause
import com.mysema.query.sql.dml.SQLUpdateClause
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.sql.Connection
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.UUID

object Util {
    @JvmStatic
    fun UUIDToByte(uuid: UUID): ByteArray {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)
        return byteBuffer.array()
    }

    @JvmStatic
    fun byteToUUID(array: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(array)
        return UUID(buffer.long, buffer.long)
    }

    /**
     * Returns the damage value to be placed in the database for a specific id. This returns the given damage
     * value for any id where the damage value represents a different distinct block or item. In all other cases,
     * 0 is returned. Blocks or items whose damage value represents orientation, or lava/water level return 0. For
     * blocks whose damage value determines both the block type <i>and</i> orientation are normalized to a standard
     * value for representing that block type in the database. So far only the block type <i>log</i> and <i>log2</i>
     * fall under this category (id's 17 and 162 respectively).
     *
     * @param id The id of the block or item to be normalized
     * @param damage The given damage value for this block or item
     * @return The normalized value to be placed into the database
     */
    @JvmStatic
    fun damageValue(id: Short, damage: Short): Short {
        when (id.toInt()) {
            1,   // Stone
            3,   // Dirt
            5,   // Wooden Planks
            6,   // Saplings
            12,  // Sand
            18,  // Leaves
            19,  // Sponges
            24,  // Sandstone
            31,  // Tall Grass
            35,  // Wool
            38,  // Flowers
            43,  // Double Slabs
            44,  // Slabs
            95,  // Stained Glass
            97,  // Monster Egg Blocks
            98,  // Stone Bricks
            125, // Double New Wooden Slabs
            126, // New Wooden Slabs
            139, // Cobblestone Walls
            144, // Various Head Blocks
            155, // Quartz Blocks
            159, // Stained Clay
            160, // Stained Glass Panes
            161, // New Leaves
            168, // Prismarine Blocks
            171, // Carpet
            175, // Various Plants
            179, // Red Sandstone
            263, // Coal and Charcoal
            349, // Various Raw Fish
            350, // Various Cooked Fish
            351, // Dyes
            373, // All Potions
            383, // Mob Spawn Eggs
            397, // Various Heads
            425  // Colored Banners
            -> return damage
            17,   // Wood
            162   // New Wood
            -> return if (damage >= 8) {
                (damage - 8).toShort()
            } else if (damage >= 4) {
                (damage - 4).toShort()
            } else {
                damage
            }
            else -> return 0
        }
    }

    private val df = DecimalFormat("#,##0.00")
    @JvmStatic
    fun distanceUnits(distance: Int): String {
        var distanceM = distance / 100.0

        if (distanceM > 1000) {
            distanceM /= 1000.0
            return df.format(distanceM) + " km"
        } else {
            return df.format(distanceM) + " m"
        }
    }

    /**
     * Transform time held in seconds to human readable time
     *
     * @param seconds Length of time interval in seconds
     * @return String of time in a more human readable format, for example 219 seconds would read as "3 minutes, 39 seconds"
     */
    @JvmStatic
    fun transformTime(seconds: Int): String {
        if (seconds < 0) {
            throw IllegalArgumentException("Time must not be negative")
        }

        // Figure out the playtime in a human readable format
        val secondsInMinute = 60
        val secondsInHour = 60 * secondsInMinute
        val secondsInDay = 24 * secondsInHour
        val secondsInWeek = 7 * secondsInDay

        val weeks = seconds / secondsInWeek

        val daySeconds = seconds % secondsInWeek
        val days = daySeconds / secondsInDay

        val hourSeconds = daySeconds % secondsInDay
        val hours = hourSeconds / secondsInHour

        val minuteSeconds = hourSeconds % secondsInHour
        val minutes = minuteSeconds / secondsInMinute

        val remainingSeconds = minuteSeconds % secondsInMinute

        // Make some strings
        val weekString: String
        val dayString: String
        val hourString: String
        val minuteString: String
        val secondString: String

        // Use correct grammar, and don't use it if it's zero
        if (weeks == 1) {
            weekString = "$weeks week"
        } else if (weeks == 0) {
            weekString = ""
        } else {
            weekString = "$weeks weeks"
        }

        if (days == 1) {
            dayString = "$days day"
        } else if (days == 0) {
            dayString = ""
        } else {
            dayString = "$days days"
        }

        if (hours == 1) {
            hourString = "$hours hour"
        } else if (hours == 0) {
            hourString = ""
        } else {
            hourString = "$hours hours"
        }

        if (minutes == 1) {
            minuteString = "$minutes minute"
        } else if (minutes == 0) {
            minuteString = ""
        } else {
            minuteString = "$minutes minutes"
        }

        if (remainingSeconds == 1) {
            secondString = "$remainingSeconds second"
        } else if (remainingSeconds == 0) {
            secondString = ""
        } else {
            secondString = "$remainingSeconds seconds"
        }

        val results = ArrayList<String>()
        if (!weekString.isEmpty()) {
            results.add(weekString)
        }
        if (!dayString.isEmpty()) {
            results.add(dayString)
        }
        if (!hourString.isEmpty()) {
            results.add(hourString)
        }
        if (!minuteString.isEmpty()) {
            results.add(minuteString)
        }
        if (!secondString.isEmpty()) {
            results.add(secondString)
        }

        if (results.size == 0) {
            return "0 seconds"
        }

        val sb = StringBuilder()
        results.forEachIndexed { i, s ->
            if (i == 0) {
                sb.append(s)
            } else {
                sb.append(", ").append(s)
            }
        }

        return sb.toString()
    }

    // TODO Temporary until all the Java code is gone
    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller.
     *
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     */
    @JvmStatic
    fun <T : RelationalPath<*>> runQuery(clazz: Class<T>,
                                         insertClause: QueryRunner<T, SQLInsertClause>,
                                         updateClause: QueryRunner<T, SQLUpdateClause>,
                                         connection: Connection,
                                         plugin: StatCraft) {
        try {
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)

            try {
                val clause = plugin.databaseManager.getInsertClause(connection, path) ?: return

                insertClause.run(path, clause)
            } catch (e: QueryException) {
                val clause = plugin.databaseManager.getUpdateClause(connection, path) ?: return

                updateClause.run(path, clause)
            }

        } catch (e: Exception) {
            // By the class generator we use, this should never happen
            e.printStackTrace()
        }
    }

    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller. This also allows work to be done before the insert and update queries. The work function can return any
     * object, and this object will be passed to the insert and update functions, and in that order. Because of this, if
     * the object is modified in the insert function, these modifications will be present in the update function.
     *
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     */
    @JvmStatic
    fun <T : RelationalPath<*>, R> runQuery(clazz: Class<T>,
                                            workBefore: QueryFunction<T, R>,
                                            insertClause: QueryRunnerMap<T, SQLInsertClause, R>,
                                            updateClause: QueryRunnerMap<T, SQLUpdateClause, R>,
                                            connection: Connection,
                                            plugin: StatCraft) {
        try {
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)
            val r = workBefore.run(path, plugin.databaseManager.getNewQuery(connection))

            try {
                val clause = plugin.databaseManager.getInsertClause(connection, path) ?: return

                insertClause.run(path, clause, r)
            } catch (e: QueryException) {
                val clause = plugin.databaseManager.getUpdateClause(connection, path) ?: return

                updateClause.run(path, clause, r)
            }
        } catch (e: Exception) {
            // By the class generator we use, this should never happen
            e.printStackTrace()
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
     * @param clazz The relevant table for this query
     * @param playerId The UUID of the relevant player
     * @param worldName The UUID of the relevant world
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     */
    @JvmStatic
    fun <T : RelationalPath<*>> runQuery(clazz: Class<T>,
                                         playerId: UUID,
                                         worldName: String,
                                         insertClause: QueryIdRunner<T, SQLInsertClause>,
                                         updateClause: QueryIdRunner<T, SQLUpdateClause>,
                                         connection: Connection,
                                         plugin: StatCraft) {
        try {
            val id = plugin.databaseManager.getPlayerId(playerId)!!
            val wid = plugin.databaseManager.getWorldId(worldName)!!
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)

            try {
                val clause = plugin.databaseManager.getInsertClause(connection, path) ?: return

                insertClause.run(path, clause, id, wid)
            } catch (e: QueryException) {
                val clause = plugin.databaseManager.getUpdateClause(connection, path) ?: return

                updateClause.run(path, clause, id, wid)
            }
        } catch (e: Exception) {
            // By the class generator we use, this should never happen
            e.printStackTrace()
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
     * @param clazz The relevant table for this query
     * @param playerId The UUID of the relevant player
     * @param worldName The UUID of the relevant world
     * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     */
    @JvmStatic
    fun <T : RelationalPath<*>, R> runQuery(clazz: Class<T>,
                                            playerId: UUID,
                                            worldName: String,
                                            workBefore: QueryIdFunction<T, R>,
                                            insertClause: QueryIdRunnerMap<T, SQLInsertClause, R>,
                                            updateClause: QueryIdRunnerMap<T, SQLUpdateClause, R>,
                                            connection: Connection,
                                            plugin: StatCraft) {
        try {
            val id = plugin.databaseManager.getPlayerId(playerId)!!
            val wid = plugin.databaseManager.getWorldId(worldName)!!
            val path = clazz.getConstructor(String::class.java).newInstance(clazz.simpleName)
            val r = workBefore.run(path, plugin.databaseManager.getNewQuery(connection), id, wid)

            try {
                val clause = plugin.databaseManager.getInsertClause(connection, path) ?: return

                insertClause.run(path, clause, id, wid, r)
            } catch (e: QueryException) {
                val clause = plugin.databaseManager.getUpdateClause(connection, path) ?: return

                updateClause.run(path, clause, id, wid, r)
            }

        } catch (e: InstantiationException) {
            // By the class generator we use, this should never happen
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }
}

