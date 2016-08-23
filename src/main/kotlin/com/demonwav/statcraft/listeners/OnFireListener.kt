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
import com.demonwav.statcraft.querydsl.QOnFire
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustByBlockEvent
import org.bukkit.event.entity.EntityCombustByEntityEvent
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffectType
import java.util.HashMap
import java.util.UUID

class OnFireListener(private val plugin: StatCraft) : Listener {
    private val lastSource = HashMap<UUID, String>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFire(event: EntityDamageEvent) {
        if (event.entity is Player) {
            val cause = event.cause
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

                val uuid = event.entity.uniqueId
                val worldName = event.entity.world.name

                val source = lastSource[uuid]

                plugin.threadManager.schedule<QOnFire>(
                    uuid, worldName,
                    { o, clause, id, worldId ->
                        clause.columns(o.id, o.worldId, o.source, o.time).values(id, worldId, source, 1).execute()
                    }, { o, clause, id, worldId ->
                        clause.where(o.id.eq(id), o.worldId.eq(worldId), o.source.eq(source)).set(o.time, o.time.add(1)).execute()
                    }
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCombust(event: EntityCombustEvent) {
        if (!plugin.config.stats.onFireAnnounce) {
            return
        }

        if (event.entity is Player) {
            val uuid = event.entity.uniqueId
            if (System.currentTimeMillis() / 1000 - plugin.getLastFireTime(uuid) > 60) {
                val giveWarning = (event.entity as Player).activePotionEffects.any { it.type == PotionEffectType.FIRE_RESISTANCE }
                if (giveWarning) {
                    event.entity.server.broadcastMessage(
                        ChatColor.RED.toString() +
                        plugin.config.stats.onFireAnnounceMessage.replace(
                            "~".toRegex(),
                            (event.entity as Player).displayName + ChatColor.RED)
                    )
                    plugin.setLastFireTime(uuid, (System.currentTimeMillis() / 1000).toInt())
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCombustByBlock(event: EntityCombustByBlockEvent) {
        if (event.entity is Player) {
            val player = event.entity as Player

            lastSource[player.uniqueId] = "BLOCK"
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCombustByEntity(event: EntityCombustByEntityEvent) {
        if (event.entity is Player) {
            val player = event.entity as Player

            if (event.combuster is Player) {
                val id = plugin.databaseManager.getPlayerId(event.combuster.uniqueId) ?: return
                lastSource.put(player.uniqueId, id.toString())
            } else {
                lastSource.put(player.uniqueId, event.combuster.name)
            }
        }
    }
}
