/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

enum class ColorCode(val code: Byte) {

    WHITE       (0.toByte()),
    ORANGE      (1.toByte()),
    MAGENTA     (2.toByte()),
    LIGHT_BLUE  (3.toByte()),
    YELLOW      (4.toByte()),
    LIME        (5.toByte()),
    PINK        (6.toByte()),
    GRAY        (7.toByte()),
    LIGHT_GRAY  (8.toByte()),
    CYAN        (9.toByte()),
    PURPLE      (10.toByte()),
    BLUE        (11.toByte()),
    BROWN       (12.toByte()),
    GREEN       (13.toByte()),
    RED         (14.toByte()),
    BLACK       (15.toByte());

    companion object {
        @JvmStatic
        fun fromCode(code: Byte): ColorCode? {
            for (colorCode in values()) {
                if (code == colorCode.code)
                    return colorCode
            }
            return null
        }
    }
}
