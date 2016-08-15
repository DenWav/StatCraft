/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

enum class BucketCode(val code: Byte) {

    WATER   (0.toByte()),
    LAVA    (1.toByte()),
    MILK    (2.toByte());

    companion object {
        @JvmStatic
        fun fromCode(code: Byte): BucketCode? {
            for (bucketCode in values()) {
                if (code == bucketCode.code)
                    return bucketCode
            }
            return null
        }
    }
}
