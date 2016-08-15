/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.magic

import org.apache.commons.lang.WordUtils
import org.bukkit.entity.Entity
import org.bukkit.entity.Skeleton

enum class EntityCode {

    OTHER,
    SKELETON,
    WITHER_SKELETON;

    fun getName(name: String): String {
        when (this) {
            OTHER -> return name
            SKELETON, WITHER_SKELETON -> return WordUtils.capitalizeFully(this.name.replace('_', ' '))
        }
    }

    companion object {
        @JvmStatic
        fun fromEntity(entity: Entity): EntityCode {
            if (entity is Skeleton) {
                if (entity.skeletonType == Skeleton.SkeletonType.NORMAL)
                    return EntityCode.SKELETON
                else if (entity.skeletonType == Skeleton.SkeletonType.WITHER)
                    return EntityCode.WITHER_SKELETON
            }
            return EntityCode.OTHER
        }
    }
}

