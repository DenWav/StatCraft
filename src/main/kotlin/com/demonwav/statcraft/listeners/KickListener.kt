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
import com.demonwav.statcraft.querydsl.QKicks
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

class KickListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerKick(event: PlayerKickEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val reason = event.reason

        plugin.threadManager.schedule<QKicks>(
            uuid, worldName,
            { k, clause, id, worldId ->
                clause.columns(k.id, k.worldId, k.reason, k.amount).values(id, worldId, reason, 1).execute()
            }, { k, clause, id, worldId ->
                clause.where(k.id.eq(id), k.worldId.eq(worldId), k.reason.eq(reason)).set(k.amount, k.amount.add(1)).execute()
            }
        )
    }
}
