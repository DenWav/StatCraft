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
import com.demonwav.statcraft.querydsl.QSleep
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent

class SleepyTimeListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBedEnter(event: PlayerBedEnterEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val currentTime = (System.currentTimeMillis() / 1000).toInt()

        plugin.threadManager.schedule<QSleep>(
            uuid, worldName,
            { s, clause, id, worldId ->
                clause.columns(s.id, s.worldId, s.enterBed).values(id, worldId, currentTime).execute()
            }, { s, clause, id, worldId ->
                clause.where(s.id.eq(id), s.worldId.eq(worldId)).set(s.enterBed, currentTime).execute()
            }
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBedLeave(event: PlayerBedLeaveEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val currentTime = (System.currentTimeMillis() / 1000).toInt()

        plugin.threadManager.schedule<QSleep>(
            uuid, worldName,
            { l, clause, id, worldId ->
                clause.columns(l.id, l.worldId, l.leaveBed).values(id, worldId, currentTime).execute()
            }, { l, clause, id, worldId ->
                clause.where(l.id.eq(id), l.worldId.eq(worldId)).set(l.leaveBed, currentTime).execute()
            }
        )

        plugin.threadManager.schedule<QSleep, Int>(
            uuid, worldName,
            { s, query, id, worldId ->
                val enterBed = query.from(s).where(s.id.eq(id), s.worldId.eq(worldId)).uniqueResult(s.enterBed) ?: 0

                currentTime - enterBed
            },
            { s, clause, id, worldId, timeSlept ->
                if (timeSlept != currentTime) {
                    clause.columns(s.id, s.timeSlept).values(id, timeSlept).execute()
                }
            }, { s, clause, id, worldId, timeSlept ->
                if (timeSlept != currentTime) {
                    clause.where(s.id.eq(id), s.worldId.eq(worldId)).set(s.timeSlept, s.timeSlept.add(timeSlept)).execute()
                }
            }
        )
    }
}
