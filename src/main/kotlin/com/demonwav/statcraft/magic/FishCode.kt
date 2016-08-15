/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

enum class FishCode(val code: Byte) {

    FISH    (0.toByte()),
    TREASURE(1.toByte()),
    JUNK    (2.toByte());

    companion object {
        @JvmStatic
        fun fromCode(code: Byte): FishCode? {
            for (fishCode in values()) {
                if (code == fishCode.code)
                    return fishCode
            }
            return null
        }
    }
}
