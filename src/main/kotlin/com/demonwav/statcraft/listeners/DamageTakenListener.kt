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
import com.demonwav.statcraft.querydsl.QDamageTaken
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageTakenListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamageTaken(event: EntityDamageEvent) {
        if (event.entity is Player && event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            val uuid = event.entity.uniqueId
            val worldName = event.entity.world.name
            val damageTaken = Math.round(event.finalDamage).toInt()

            plugin.threadManager.schedule<QDamageTaken>(
                uuid, worldName,
                { t, clause, id, worldId ->
                    clause.columns(t.id, t.worldId, t.entity, t.amount)
                        .values(id, worldId, event.cause.name, damageTaken).execute()
                }, { t, clause, id, worldId ->
                    clause.where(t.id.eq(id), t.worldId.eq(worldId), t.entity.eq(event.cause.name))
                        .set(t.amount, t.amount.add(damageTaken)).execute()
                }
            )

            // DROWN ANNOUNCE
            if (plugin.config.stats.drowningAnnounce)
                if (event.cause == EntityDamageEvent.DamageCause.DROWNING) {
                    if (System.currentTimeMillis() / 1000 - plugin.getLastDrownTime(uuid) > 120) {

                        event.entity.server.broadcastMessage(
                            ChatColor.BLUE.toString() +
                            plugin.config.stats.drownAnnounceMessage.replace(
                                "~".toRegex(),
                                (event.entity as Player).displayName + ChatColor.BLUE
                            )
                        )
                        plugin.setLastDrowningTime(uuid, (System.currentTimeMillis() / 1000).toInt())
                    }
                }
            // POISON ANNOUNCE
            if (plugin.config.stats.poisonAnnounce)
                if (event.cause == EntityDamageEvent.DamageCause.POISON) {
                    if (System.currentTimeMillis() / 1000 - plugin.getLastPoisonTime(uuid) > 120) {

                        event.entity.server.broadcastMessage(
                            ChatColor.GREEN.toString() +
                            plugin.config.stats.poisonAnnounceMessage.replace(
                                "~".toRegex(),
                                (event.entity as Player).displayName + ChatColor.GREEN
                            )
                        )
                        plugin.setLastPoisonTime(uuid, (System.currentTimeMillis() / 1000).toInt())
                    }
                }
            // WITHER ANNOUNCE
            if (plugin.config.stats.witherAnnounce)
                if (event.cause == EntityDamageEvent.DamageCause.WITHER) {
                    if (System.currentTimeMillis() / 1000 - plugin.getLastWitherTime(uuid) > 120) {

                        event.entity.server.broadcastMessage(
                            ChatColor.DARK_GRAY.toString() +
                            plugin.config.stats.witherAnnounceMessage.replace(
                                "~".toRegex(),
                                (event.entity as Player).displayName + ChatColor.DARK_GRAY
                            )
                        )
                        plugin.setLastWitherTime(uuid, (System.currentTimeMillis() / 1000).toInt())
                    }
                }
        }
    }
}
