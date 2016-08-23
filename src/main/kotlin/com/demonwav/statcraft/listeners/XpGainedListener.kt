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
import com.demonwav.statcraft.querydsl.QXpGained
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

class XpGainedListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onXpGain(event: PlayerExpChangeEvent) {
        val amount = event.amount
        if (amount > 0) {
            val uuid = event.player.uniqueId
            val worldName = event.player.world.name

            plugin.threadManager.schedule<QXpGained>(
                uuid, worldName,
                { x, clause, id, worldId ->
                    clause.columns(x.id, x.worldId, x.amount).values(id, worldId, amount).execute()
                }, { x, clause, id, worldId ->
                    clause.where(x.id.eq(id), x.worldId.eq(worldId)).set(x.amount, x.amount.add(amount)).execute()
                }
            )
        }
    }
}
