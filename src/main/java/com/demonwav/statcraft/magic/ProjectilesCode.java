/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic;

public enum ProjectilesCode {

    NORMAL_ARROW((short)0),
    FLAMING_ARROW((short)1),
    ENDER_PEARL((short)2),
    UNHATCHED_EGG((short)3),
    HATCHED_EGG((short)4),
    SNOWBALL((short)5),
    FOUR_HATCHED_EGG((short)6);

    private short code;

    ProjectilesCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static ProjectilesCode fromCode(short code) {
        for (ProjectilesCode projectilesCode : values()) {
            if (code == projectilesCode.getCode())
                return projectilesCode;
        }
        return null;
    }
}
