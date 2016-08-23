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
import com.demonwav.statcraft.querydsl.QHighestLevel
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLevelChangeEvent

class HighestLevelListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onLevel(event: PlayerLevelChangeEvent) {
        val newLevel = event.newLevel
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name

        plugin.threadManager.schedule<QHighestLevel>(
            uuid, worldName,
            { h, clause, id, worldId ->
                clause.columns(h.id, h.level).values(id, newLevel).execute()
            }, { h, clause, id, worldId ->
                clause.where(h.id.eq(id), h.level.lt(newLevel)).set(h.level, newLevel).execute()
            }
        )
    }
}
