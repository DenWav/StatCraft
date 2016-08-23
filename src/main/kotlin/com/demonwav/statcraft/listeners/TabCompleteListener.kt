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
import com.demonwav.statcraft.querydsl.QTabComplete
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatTabCompleteEvent

class TabCompleteListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTabComplete(event: PlayerChatTabCompleteEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name

        plugin.threadManager.schedule<QTabComplete>(
            uuid, worldName,
            { t, clause, id, worldId ->
                clause.columns(t.id, t.worldId, t.amount).values(id, worldId, 1).execute()
            }, { t, clause, id, worldId ->
                clause.where(t.id.eq(id), t.worldId.eq(worldId)).set(t.amount, t.amount.add(1)).execute()
            }
        )
    }
}
