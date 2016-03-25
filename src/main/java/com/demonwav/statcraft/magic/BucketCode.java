/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic;

public enum BucketCode {

    WATER((byte)0),
    LAVA((byte)1),
    MILK((byte)2);

    private byte code;

    BucketCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static BucketCode fromCode(byte code) {
        for (BucketCode bucketCode : values()) {
            if (code == bucketCode.getCode())
                return bucketCode;
        }
        return null;
    }
}
