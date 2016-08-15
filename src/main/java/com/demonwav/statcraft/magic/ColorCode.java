/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic;


public enum ColorCode {
    WHITE       ((byte)0),
    ORANGE      ((byte)1),
    MAGENTA     ((byte)2),
    LIGHT_BLUE  ((byte)3),
    YELLOW      ((byte)4),
    LIME        ((byte)5),
    PINK        ((byte)6),
    GRAY        ((byte)7),
    LIGHT_GRAY  ((byte)8),
    CYAN        ((byte)9),
    PURPLE      ((byte)10),
    BLUE        ((byte)11),
    BROWN       ((byte)12),
    GREEN       ((byte)13),
    RED         ((byte)14),
    BLACK       ((byte)15);

    private byte code;

    ColorCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static ColorCode fromCode(byte code) {
        for (ColorCode colorCode : values()) {
            if (code == colorCode.getCode())
                return colorCode;
        }
        return null;
    }
}
