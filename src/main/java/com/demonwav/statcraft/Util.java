/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.magic.BucketCode;
import com.demonwav.statcraft.magic.ProjectilesCode;
import com.demonwav.statcraft.querydsl.QProjectiles;
import com.mysema.query.QueryException;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.expr.CaseBuilder;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public class Util {

    public static byte[] UUIDToByte(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID byteToUUID(byte[] array) {
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
            case 161:   // New LEaves
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

    public static void set(
            final StatCraft plugin,
            final RelationalPathBase<?> base,
            final NumberPath<Integer> id,
            final NumberPath<Integer> amount,
            final int idVal,
            final int set) {
        plugin.getThreadManager().schedule(base.getType(), new Runnable() {
            @Override
            public void run() {
                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(base);

                    if (clause == null)
                        return;

                    clause.columns(id, amount).values(idVal, set).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(base);

                    if (clause == null)
                        return;

                    clause.where(id.eq(idVal)).set(amount, set).execute();
                }
            }
        });
    }

    public static void increment(
            final StatCraft plugin,
            final RelationalPathBase<?> base,
            final NumberPath<Integer> id,
            final NumberPath<Integer> amount,
            final int idVal,
            final int inc) {
        try {
            // INSERT
            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(base);

            if (clause == null)
                return;

            clause.columns(id, amount).values(id, inc).execute();
        } catch (QueryException e) {
            // UPDATE
            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(base);

            if (clause == null)
                return;

            clause.where(id.eq(idVal)).set(amount, amount.add(inc)).execute();
        }
    }

    public static void max(
            final StatCraft plugin,
            final RelationalPathBase<?> base,
            final NumberPath<Integer> id,
            final NumberPath<Short> type,
            final NumberPath<Integer> amount,
            final NumberPath<Integer> totalDistance,
            final NumberPath<Integer> maxThrow,
            final int idVal,
            final short typeVal,
            final int finalDistanceVal) {
        try {
            // INSERT
            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(base);

            if (clause == null)
                return;

            clause.columns(id, type, amount, totalDistance, maxThrow)
                    .values(idVal, typeVal, 1, finalDistanceVal, finalDistanceVal).execute();
        } catch (QueryException ex) {
            // UPDATE
            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(base);

            if (clause == null)
                return;

            clause.where(id.eq(idVal), type.eq(typeVal)).set(amount, amount.add(1))
                    .set(totalDistance, totalDistance.add(finalDistanceVal))
                    .set(maxThrow,
                            new CaseBuilder()
                                    .when(maxThrow.lt(finalDistanceVal)).then(finalDistanceVal)
                                    .otherwise(maxThrow))
                    .execute();
        }
    }

    public static void projectile(StatCraft plugin, final QProjectiles p, final int id, final ProjectilesCode code, final int finalDistance) {
        max(plugin, p, p.id, p.type, p.amount, p.totalDistance, p.maxThrow, id, code.getCode(), finalDistance);
    }

    public static void bucket(
            final StatCraft plugin,
            final RelationalPathBase<?> base,
            final NumberPath<Integer> id,
            final NumberPath<Byte> type,
            final NumberPath<Integer> amount,
            final int idVal,
            final BucketCode code) {
        try {
            // INSERT
            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(base);

            if (clause == null)
                return;

            clause.columns(id, type, amount)
                    .values(idVal, code.getCode(), 1).execute();
        } catch (QueryException ex) {
            // UPDATE
            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(base);

            if (clause == null)
                return;

            clause.where(
                    id.eq(idVal),
                    type.eq(code.getCode())
            ).set(amount, amount.add(1)).execute();
        }
    }

    public static void damage(
            final StatCraft plugin,
            final RelationalPathBase<?> base,
            final NumberPath<Integer> id,
            final StringPath entity,
            final NumberPath<Integer> amount,
            final int idVal,
            final String entityVal,
            final int damage) {
        try {
            // INSERT
            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(base);
            if (clause == null)
                return;
            clause.columns(id, entity, amount)
                    .values(idVal, entityVal, damage).execute();
        } catch (QueryException e) {
            // UPDATE
            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(base);
            if (clause == null)
                return;
            clause.where(
                    id.eq(id),
                    entity.eq(entityVal)
            ).set(amount, amount.add(damage)).execute();
        }
    }
}
