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

class SnowballListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSnowball(event: ProjectileHitEvent) {
        if (event.entity.type == EntityType.SNOWBALL && event.entity.shooter is Player) {
            val player = event.entity.shooter as Player
            val playerLocation = player.location
            val snowballLocation = event.entity.location

            val distance = playerLocation.distance(snowballLocation)
            val finalDistance = Math.round(distance * 100.0).toInt()

            val uuid = player.uniqueId
            val worldName = player.world.name

            plugin.threadManager.schedule<QProjectiles>(
                uuid, worldName,
                { p, clause, id, worldId ->
                    clause.columns(p.id, p.worldId, p.type, p.amount, p.totalDistance, p.maxThrow)
                        .values(id, worldId, ProjectilesCode.SNOWBALL.code, 1, finalDistance, finalDistance).execute()
                }, { p, clause, id, worldId ->
                    clause.where(p.id.eq(id), p.worldId.eq(worldId), p.type.eq(ProjectilesCode.SNOWBALL.code))
                        .set(p.amount, p.amount.add(1))
                        .set(p.totalDistance, p.totalDistance.add(finalDistance))
                        .set(
                            p.maxThrow,
                            CaseBuilder()
                                .`when`(p.maxThrow.lt(finalDistance))
                                .then(finalDistance)
                                .otherwise(p.maxThrow)
                        ).execute()
                }
            )
        }
    }
}
