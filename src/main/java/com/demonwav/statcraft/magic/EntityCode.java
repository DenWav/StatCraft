/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic;

import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;

public enum EntityCode {

    OTHER,
    SKELETON,
    WITHER_SKELETON;

    public static EntityCode fromEntity(Entity entity) {
        if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) entity;
            if (skeleton.getSkeletonType() == Skeleton.SkeletonType.NORMAL)
                return EntityCode.SKELETON;
            else if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER)
                return EntityCode.WITHER_SKELETON;
        }
        return EntityCode.OTHER;
    }

    public String getName(String name) {
        switch (this) {
            case OTHER:
                return name;
            case SKELETON:
            case WITHER_SKELETON:
                return WordUtils.capitalizeFully(this.name().replace('_', ' '));
            default:
                return "";
        }
    }
}
