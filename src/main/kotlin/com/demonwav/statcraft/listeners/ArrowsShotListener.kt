/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.magic.ProjectilesCode
import com.demonwav.statcraft.querydsl.QProjectiles
import com.mysema.query.types.expr.CaseBuilder
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class ArrowsShotListener(private val plugin: StatCraft) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onArrowShot(event: ProjectileHitEvent) {
        if (event.entity?.shooter is Player && event.entity?.type == EntityType.ARROW) {
            val uuid = (event.entity.shooter as Player).uniqueId
            val worldName = (event.entity.shooter as Player).world.name
            val code: ProjectilesCode
            if (event.entity.fireTicks > 0) {
                code = ProjectilesCode.FLAMING_ARROW
            } else {
                code = ProjectilesCode.NORMAL_ARROW
            }
            val playerLocation = (event.entity.shooter as Player).location
            val arrowLocation = event.entity.location

            val distance = playerLocation.distance(arrowLocation)
            val finalDistance = Math.round(distance * 100.0).toInt()

            plugin.threadManager.schedule(
                QProjectiles::class.java, uuid, worldName,
                { p, clause, id, worldId ->
                    clause.columns(p.id, p.worldId, p.type, p.amount, p.totalDistance, p.maxThrow)
                        .values(id, worldId, code.code, 1, finalDistance, finalDistance).execute()
                }, { p, clause, id, worldId ->
                    clause.where(p.id.eq(id), p.worldId.eq(worldId), p.type.eq(code.code))
                        .set(p.amount, p.amount.add(1))
                        .set(p.totalDistance, p.totalDistance.add(finalDistance))
                        .set(p.maxThrow,
                            CaseBuilder()
                                .`when`(p.maxThrow.lt(finalDistance)).then(finalDistance)
                                .otherwise(p.maxThrow))
                        .execute()
                }
            )
        }
    }
}
