/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

import org.bukkit.potion.PotionEffectType

enum class PotionEffectCode(val effect: PotionEffectType, val code: Int) {

    SPEED               (PotionEffectType.SPEED,                0),
    SLOWNESS            (PotionEffectType.SLOW,                 1),
    HASTE               (PotionEffectType.FAST_DIGGING,         2),
    MINING_FATIGUE      (PotionEffectType.SLOW_DIGGING,         3),
    STRENGTH            (PotionEffectType.INCREASE_DAMAGE,      4),
    JUMP_BOOST          (PotionEffectType.JUMP,                 5),
    NAUSEA              (PotionEffectType.CONFUSION,            6),
    REGENERATION        (PotionEffectType.REGENERATION,         7),
    RESISTANCE          (PotionEffectType.DAMAGE_RESISTANCE,    8),
    FIRST_RESISTANCE    (PotionEffectType.FIRE_RESISTANCE,      9),
    WATER_BREATHING     (PotionEffectType.WATER_BREATHING,      10),
    INVISIBILITY        (PotionEffectType.INVISIBILITY,         11),
    BLINDNESS           (PotionEffectType.BLINDNESS,            12),
    NIGHT_VISION        (PotionEffectType.NIGHT_VISION,         13),
    HUNGER              (PotionEffectType.HUNGER,               14),
    WEAKNESS            (PotionEffectType.WEAKNESS,             15),
    POISON              (PotionEffectType.POISON,               16),
    WITHER              (PotionEffectType.WITHER,               17),
    HEALTH_BOOST        (PotionEffectType.HEALTH_BOOST,         18),
    ABSORPTION          (PotionEffectType.ABSORPTION,           19),
    SATURATION          (PotionEffectType.SATURATION,           20),
    GLOWING             (PotionEffectType.GLOWING,              21),
    LEVITATION          (PotionEffectType.LEVITATION,           22),
    LUCK                (PotionEffectType.LUCK,                 23),
    BAD_LUCK            (PotionEffectType.UNLUCK,               24);

    companion object {
        @JvmStatic
        fun fromEffect(type: PotionEffectType): PotionEffectCode? {
            for (potionEffectCode in values()) {
                if (type === potionEffectCode.effect)
                    return potionEffectCode
            }
            return null
        }

        @JvmStatic
        fun fromCode(code: Int): PotionEffectCode? {
            for (potionEffectCode in values()) {
                if (code == potionEffectCode.code)
                    return potionEffectCode
            }
            return null
        }
    }
}
