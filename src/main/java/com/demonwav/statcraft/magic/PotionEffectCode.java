package com.demonwav.statcraft.magic;

import org.bukkit.potion.PotionEffectType;

public enum PotionEffectCode {

    SPEED           (PotionEffectType.SPEED,                0),
    SLOWNESS        (PotionEffectType.SLOW,                 1),
    HASTE           (PotionEffectType.FAST_DIGGING,         2),
    MINING_FATIGUE  (PotionEffectType.SLOW_DIGGING,         3),
    STRENGTH        (PotionEffectType.INCREASE_DAMAGE,      4),
    JUMP_BOOST      (PotionEffectType.JUMP,                 5),
    NAUSEA          (PotionEffectType.CONFUSION,            6),
    REGENERATION    (PotionEffectType.REGENERATION,         7),
    RESISTANCE      (PotionEffectType.DAMAGE_RESISTANCE,    8),
    FIRST_RESISTANCE(PotionEffectType.FIRE_RESISTANCE,      9),
    WATER_BREATHING (PotionEffectType.WATER_BREATHING,      10),
    INVISIBILITY    (PotionEffectType.INVISIBILITY,         11),
    BLINDNESS       (PotionEffectType.BLINDNESS,            12),
    NIGHT_VISION    (PotionEffectType.NIGHT_VISION,         13),
    HUNGER          (PotionEffectType.HUNGER,               14),
    WEAKNESS        (PotionEffectType.WEAKNESS,             15),
    POISON          (PotionEffectType.POISON,               16),
    WITHER          (PotionEffectType.WITHER,               17),
    HEALTH_BOOST    (PotionEffectType.HEALTH_BOOST,         18),
    ABSORPTION      (PotionEffectType.ABSORPTION,           19),
    SATURATION      (PotionEffectType.SATURATION,           20),
    GLOWING         (PotionEffectType.GLOWING,              21),
    LEVITATION      (PotionEffectType.LEVITATION,           22),
    LUCK            (PotionEffectType.LUCK,                 23),
    BAD_LUCK        (PotionEffectType.UNLUCK,               24);

    final private PotionEffectType effect;
    final private int code;

    PotionEffectCode(final PotionEffectType effect, final int code) {
        this.effect = effect;
        this.code = code;
    }

    public PotionEffectType getEffect() {
        return effect;
    }

    public int getCode() {
        return code;
    }

    public static PotionEffectCode fromEffect(PotionEffectType type) {
        for (PotionEffectCode potionEffectCode : values()) {
            if (type == potionEffectCode.getEffect())
                return potionEffectCode;
        }
        return null;
    }

    public static PotionEffectCode fromCode(int code) {
        for (PotionEffectCode potionEffectCode : values()) {
            if (code == potionEffectCode.getCode())
                return potionEffectCode;
        }
        return null;
    }
}
