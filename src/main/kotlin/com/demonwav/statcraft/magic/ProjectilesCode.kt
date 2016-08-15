/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

enum class ProjectilesCode(val code: Short) {

    NORMAL_ARROW    (0.toShort()),
    FLAMING_ARROW   (1.toShort()),
    ENDER_PEARL     (2.toShort()),
    UNHATCHED_EGG   (3.toShort()),
    HATCHED_EGG     (4.toShort()),
    SNOWBALL        (5.toShort()),
    FOUR_HATCHED_EGG(6.toShort());

    companion object {
        @JvmStatic
        fun fromCode(code: Short): ProjectilesCode? {
            for (projectilesCode in values()) {
                if (code == projectilesCode.code)
                    return projectilesCode
            }
            return null
        }
    }
}
