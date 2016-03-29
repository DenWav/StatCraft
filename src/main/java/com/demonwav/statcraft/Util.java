/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.sql.QueryFunction;
import com.demonwav.statcraft.sql.QueryIdFunction;
import com.demonwav.statcraft.sql.QueryIdRunner;
import com.demonwav.statcraft.sql.QueryIdRunnerMap;
import com.demonwav.statcraft.sql.QueryRunner;
import com.demonwav.statcraft.sql.QueryRunnerMap;
import com.mysema.query.QueryException;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public final class Util {

    @NotNull
    public static byte[] UUIDToByte(@NotNull  UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @NotNull
    public static UUID byteToUUID(@NotNull  byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return new UUID(buffer.getLong(), buffer.getLong());
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
    public static short damageValue(short id, short damage) {
        switch (id) {
            case 1:     // Stone
            case 3:     // Dirt
            case 5:     // Wooden Planks
            case 6:     // Saplings
            case 12:    // Sand
            case 18:    // Leaves
            case 19:    // Sponges
            case 24:    // Sandstone
            case 31:    // Tall Grass
            case 35:    // Wool
            case 38:    // Flowers
            case 43:    // Double Slabs
            case 44:    // Slabs
            case 95:    // Stained Glass
            case 97:    // Monster Egg Blocks
            case 98:    // Stone Bricks
            case 125:   // Double New Wooden Slabs
            case 126:   // New Wooden Slabs
            case 139:   // Cobblestone Walls
            case 144:   // Various Head Blocks
            case 155:   // Quartz Blocks
            case 159:   // Stained Clay
            case 160:   // Stained Glass Panes
            case 161:   // New Leaves
            case 168:   // Prismarine Blocks
            case 171:   // Carpet
            case 175:   // Various Plants
            case 179:   // Red Sandstone
            case 263:   // Coal and Charcoal
            case 349:   // Various Raw Fish
            case 350:   // Various Cooked Fish
            case 351:   // Dyes
            case 373:   // All Potions
            case 383:   // Mob Spawn Eggs
            case 397:   // Various Heads
            case 425:   // Colored Banners
                return damage;
            case 17:    // Wood
            case 162:   // New Wood
                return (short) ((damage >= 8) ? (damage - 8) : (damage >= 4) ? (damage - 4) : damage);
            default:
                return 0;
        }
    }

    public static String distanceUnits(int distance) {
        double distanceM = distance / 100.0;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        if (distanceM > 1000) {
            distanceM /= 1000;
            return df.format(distanceM) + " km";
        } else {
            return df.format(distanceM) + " m";
        }
    }

    /**
     * Transform time held in seconds to human readable time
     *
     * @param seconds Length of time interval in seconds
     * @return String of time in a more human readable format, for example 219 seconds would read as "3 minutes, 39 seconds"
     */
    public static String transformTime(int seconds) {
        if (seconds < 0)
            throw new IllegalArgumentException("Time must not be negative");

        // Figure out the playtime in a human readable format
        final int secondsInMinute = 60;
        final int secondsInHour = 60 * secondsInMinute;
        final int secondsInDay = 24 * secondsInHour;
        final int secondsInWeek = 7 * secondsInDay;

        final int weeks = seconds / secondsInWeek;

        final int daySeconds = seconds % secondsInWeek;
        final int days = daySeconds / secondsInDay;

        final int hourSeconds = daySeconds % secondsInDay;
        final int hours = hourSeconds / secondsInHour;

        final int minuteSeconds = hourSeconds % secondsInHour;
        final int minutes = minuteSeconds / secondsInMinute;

        final int remainingSeconds = minuteSeconds % secondsInMinute;

        // Make some strings
        String weekString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;

        // Use correct grammar, and don't use it if it's zero
        if (weeks == 1)
            weekString = weeks + " week";
        else if (weeks == 0)
            weekString = "";
        else
            weekString = weeks + " weeks";

        if (days == 1)
            dayString = days + " day";
        else if (days == 0)
            dayString = "";
        else
            dayString = days + " days";

        if (hours == 1)
            hourString = hours + " hour";
        else if (hours == 0)
            hourString = "";
        else
            hourString = hours + " hours";

        if (minutes == 1)
            minuteString = minutes + " minute";
        else if (minutes == 0)
            minuteString = "";
        else
            minuteString = minutes + " minutes";

        if (remainingSeconds == 1)
            secondString = remainingSeconds + " second";
        else if (remainingSeconds == 0)
            secondString = "";
        else
            secondString = remainingSeconds + " seconds";

        ArrayList<String> results = new ArrayList<>();
        results.add(weekString);
        results.add(dayString);
        results.add(hourString);
        results.add(minuteString);
        results.add(secondString);

        for (int x = results.size() - 1; x >= 0; x--) {
            if (results.get(x).equals("")) {
                results.remove(x);
            }
        }

        if (results.size() == 0)
            return "0 seconds";

        String finalResult = "";
        for (int x = 0; x < results.size(); x++) {
            if (x == results.size() - 1) {
                if (x == 0)
                    finalResult = results.get(x);
                else
                    finalResult = finalResult + ", " + results.get(x);
            } else {
                if (x == 0)
                    finalResult = results.get(x);
                else
                    finalResult = finalResult + ", " + results.get(x);
            }
        }

        return finalResult;
    }

    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller.
     * <p>
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     * @param <T> The RelationalPath that represents the relevant table
     */
    public  static <T extends RelationalPath<?>> void runQuery(final Class<T> clazz,
                                                               final QueryRunner<T, SQLInsertClause> insertClause,
                                                               final QueryRunner<T, SQLUpdateClause> updateClause,
                                                               final Connection connection,
                                                               final StatCraft plugin) {
        try {
            final T path = clazz.getConstructor(String.class).newInstance(clazz.getSimpleName());

            try {
                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(connection, path);

                if (clause == null)
                    return;

                insertClause.run(path, clause);
            } catch (QueryException e) {
                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(connection, path);

                if (clause == null)
                    return;

                updateClause.run(path, clause);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // By the class generator we use, this should never happen
            e.printStackTrace();
        }
    }

    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller. This also allows work to be done before the insert and update queries. The work function can return any
     * object, and this object will be passed to the insert and update functions, and in that order. Because of this, if
     * the object is modified in the insert function, these modifications will be present in the update function.
     * <p>
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     * @param <T> The RelationalPath that represents the relevant table
     */
    public static <T extends RelationalPath<?>, R> void runQuery(final Class<T> clazz,
                                                                 final QueryFunction<T, R> workBefore,
                                                                 final QueryRunnerMap<T, SQLInsertClause, R> insertClause,
                                                                 final QueryRunnerMap<T, SQLUpdateClause, R> updateClause,
                                                                 final Connection connection,
                                                                 final StatCraft plugin) {
        try {
            final T path = clazz.getConstructor(String.class).newInstance(clazz.getSimpleName());
            final R r = workBefore.run(path, plugin.getDatabaseManager().getNewQuery(connection));

            try {
                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(connection, path);

                if (clause == null)
                    return;

                insertClause.run(path, clause, r);
            } catch (QueryException e) {
                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(connection, path);

                if (clause == null)
                    return;

                updateClause.run(path, clause, r);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // By the class generator we use, this should never happen
            e.printStackTrace();
        }
    }

    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller.
     * <p>
     * For convenience this method also allows a player's UUID and world UUID to be passed in. The database id of the
     * player and world will be fetched before the insert and update functions are called, and the id will be passed to
     * them. This is not an expensive operation as both of these values are cached.
     * <p>
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param playerId The UUID of the relevant player
     * @param worldName The UUID of the relevant world
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     * @param <T> The RelationalPath that represents the relevant table
     */
    public static <T extends RelationalPath<?>> void runQuery(final Class<T> clazz,
                                                              final UUID playerId,
                                                              final String worldName,
                                                              final QueryIdRunner<T, SQLInsertClause> insertClause,
                                                              final QueryIdRunner<T, SQLUpdateClause> updateClause,
                                                              final Connection connection,
                                                              final StatCraft plugin) {
        try {
            final int id = plugin.getDatabaseManager().getPlayerId(playerId);
            final int wid = plugin.getDatabaseManager().getWorldId(worldName);
            final T path = clazz.getConstructor(String.class).newInstance(clazz.getSimpleName());

            try {
                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(connection, path);

                if (clause == null)
                    return;

                insertClause.run(path, clause, id, wid);
            } catch (QueryException e) {
                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(connection, path);

                if (clause == null)
                    return;

                updateClause.run(path, clause, id, wid);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // By the class generator we use, this should never happen
            e.printStackTrace();
        }
    }

    /**
     * Run an insert/update query on the given table. This method handles the clause object creation and fall-through
     * to update when insert fails. This method run on the thread it is called, all threading must be managed by the
     * caller. This also allows work to be done before the insert and update queries. The work function can return any
     * object, and this object will be passed to the insert and update functions, and in that order. Because of this, if
     * the object is modified in the insert function, these modifications will be present in the update function.
     * <p>
     * For convenience this method also allows a player's UUID and a world's UUID to be passed in. The database id of
     * the player and world will be fetched before the insert and update functions are called, and the id will be
     * passed to them. This is not an expensive operation as both of these values are cached.
     * <p>
     * Thanks to type inferencing no type parameters should need to be explicitly provided.
     *
     * @param clazz The relevant table for this query
     * @param playerId The UUID of the relevant player
     * @param worldName The UUID of the relevant world
     * @param workBefore The action to run before the queries, returning an object which will be passed to the two queries
     * @param insertClause The action to run for the insert query
     * @param updateClause The action to run for the update query if the insert fails
     * @param plugin The StatCraft object
     * @param <T> The RelationalPath that represents the relevant table
     */
    public static <T extends RelationalPath<?>, R> void runQuery(final Class<T> clazz,
                                                                 final UUID playerId,
                                                                 final String worldName,
                                                                 final QueryIdFunction<T, R> workBefore,
                                                                 final QueryIdRunnerMap<T, SQLInsertClause, R> insertClause,
                                                                 final QueryIdRunnerMap<T, SQLUpdateClause, R> updateClause,
                                                                 final Connection connection,
                                                                 final StatCraft plugin) {
        try {
            final int id = plugin.getDatabaseManager().getPlayerId(playerId);
            final int wid = plugin.getDatabaseManager().getWorldId(worldName);
            final T path = clazz.getConstructor(String.class).newInstance(clazz.getSimpleName());
            final R r = workBefore.run(path, plugin.getDatabaseManager().getNewQuery(connection), id, wid);

            try {
                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(connection, path);

                if (clause == null)
                    return;

                insertClause.run(path, clause, id, wid, r);
            } catch (QueryException e) {
                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(connection, path);

                if (clause == null)
                    return;

                updateClause.run(path, clause, id, wid, r);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // By the class generator we use, this should never happen
            e.printStackTrace();
        }
    }
}
