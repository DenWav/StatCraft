/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

import org.bukkit.Statistic

enum class MoveCode(val code: Byte, val stat: Statistic) {

    WALKING     (0.toByte(),    Statistic.WALK_ONE_CM),
    CROUCHING   (1.toByte(),    Statistic.CROUCH_ONE_CM),
    SPRINTING   (2.toByte(),    Statistic.SPRINT_ONE_CM),
    SWIMMING    (3.toByte(),    Statistic.SWIM_ONE_CM),
    FALLING     (4.toByte(),    Statistic.FALL_ONE_CM),
    CLIMBING    (5.toByte(),    Statistic.CLIMB_ONE_CM),
    FLYING      (6.toByte(),    Statistic.FLY_ONE_CM),
    DIVING      (7.toByte(),    Statistic.DIVE_ONE_CM),
    MINECART    (8.toByte(),    Statistic.MINECART_ONE_CM),
    BOAT        (9.toByte(),    Statistic.BOAT_ONE_CM),
    PIG         (10.toByte(),   Statistic.PIG_ONE_CM),
    HORSE       (11.toByte(),   Statistic.HORSE_ONE_CM),
    ELYTRA      (12.toByte(),   Statistic.AVIATE_ONE_CM);

    companion object {
        @JvmStatic
        fun fromCode(code: Byte): MoveCode? {
            for (moveCode in values()) {
                if (code == moveCode.code)
                    return moveCode
            }
            return null
        }
    }
}
