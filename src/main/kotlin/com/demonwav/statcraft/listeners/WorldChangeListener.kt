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
import com.demonwav.statcraft.querydsl.QWorldChange
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class WorldChangeListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerWorldChange(event: PlayerChangedWorldEvent) {
        val uuid = event.player.uniqueId
        val fromWorldName = event.from.name
        val toWorldName = event.player.world.name

        plugin.threadManager.schedule<QWorldChange, Int>(
            uuid, fromWorldName,
            { w, query, id, worldId ->
                plugin.databaseManager.getWorldId(toWorldName) ?: 0
            }, { w, clause, id, fromWorldId, toWorldId ->
                clause.columns(w.id, w.toWorld, w.fromWorld, w.amount).values(id, toWorldId, fromWorldId, 1).execute()
            }, { w, clause, id, fromWorldId, toWorldId ->
                clause.where(
                    w.id.eq(id),
                    w.toWorld.eq(toWorldId),
                    w.fromWorld.eq(fromWorldId)).set(w.amount, w.amount.add(1)).execute()
            }
        )

        plugin.moveUpdater.run(event.player, fromWorldName)
    }
}
