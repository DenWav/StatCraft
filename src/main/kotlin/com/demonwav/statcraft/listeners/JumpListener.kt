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
import com.demonwav.statcraft.querydsl.QJumps
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerStatisticIncrementEvent

class JumpListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onJump(event: PlayerStatisticIncrementEvent) {
        if (event.statistic == Statistic.JUMP) {
            val uuid = event.player.uniqueId
            val worldName = event.player.world.name

            plugin.threadManager.schedule<QJumps>(
                uuid, worldName,
                { j, clause, id, worldId ->
                    clause.columns(j.id, j.worldId, j.amount).values(id, worldId, 1).execute()
                }, { j, clause, id, worldId ->
                    clause.where(j.id.eq(id), j.worldId.eq(worldId)).set(j.amount, j.amount.add(1)).execute()
                }
            )
        }
    }
}
