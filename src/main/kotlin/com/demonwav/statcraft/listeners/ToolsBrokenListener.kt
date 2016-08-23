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
import com.demonwav.statcraft.querydsl.QToolsBroken
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent

class ToolsBrokenListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onToolBreak(event: PlayerItemBreakEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val item = event.brokenItem.type.id.toShort()

        plugin.threadManager.schedule<QToolsBroken>(
            uuid, worldName,
            { t, clause, id, worldId ->
                clause.columns(t.id, t.worldId, t.item, t.amount).values(id, worldId, item, 1).execute()
            }, { t, clause, id, worldId ->
                clause.where(t.id.eq(id), t.worldId.eq(worldId), t.item.eq(item)).set(t.amount, t.amount.add(1)).execute()
            }
        )
    }
}
