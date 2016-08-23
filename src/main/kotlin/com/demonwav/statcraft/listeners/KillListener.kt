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
import com.demonwav.statcraft.magic.EntityCode
import com.demonwav.statcraft.querydsl.QKills
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class KillListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onKill(event: EntityDeathEvent) {
        if (event.entity.killer != null) {
            val uuid = event.entity.killer.uniqueId
            val worldName = event.entity.killer.world.name
            val entity = event.entity
            val code = EntityCode.fromEntity(event.entity)

            plugin.threadManager.schedule<QKills, String>(
                uuid, worldName,
                { k, query, id, worldId ->
                    val entityValue: String
                    if (entity is Player) {
                        entityValue = plugin.databaseManager.getPlayerId(entity.getUniqueId()).toString()
                    } else {
                        if (entity is EnderPearl) {
                            entityValue = "Ender Pearl"
                        } else {
                            entityValue = code.getName(entity.name)
                        }
                    }

                    entityValue
                }, { k, clause, id, worldId, entityValue ->
                    clause.columns(k.id, k.worldId, k.entity, k.amount).values(id, worldId, entityValue, 1).execute()
                }, { k, clause, id, worldId, entityValue ->
                    clause.where(
                        k.id.eq(id),
                        k.worldId.eq(worldId),
                        k.entity.eq(entityValue)
                    ).set(k.amount, k.amount.add(1)).execute()
                }
            )
        }
    }
}
