/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners

import com.demonwav.statcraft.ServerStatUpdater
import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.querydsl.QJoins
import com.demonwav.statcraft.querydsl.QPlayTime
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QSeen
import com.demonwav.statcraft.runQuery
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayTimeListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onJoin(event: PlayerJoinEvent) {
        val name = event.player.name
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

        plugin.threadManager.scheduleRaw(QPlayers::class.java) { conn ->
            // This MUST be done before the other two jobs
            val id = plugin.setupPlayer(event.player, conn) ?: return@scheduleRaw
            val worldId = plugin.databaseManager.getWorldId(worldName) ?: return@scheduleRaw
            plugin.players[name] = uuid

            if (plugin.config.stats.joins) {
                plugin.threadManager.scheduleRaw(QJoins::class.java) { connection ->
                    QJoins.joins.runQuery(
                        { j, clause ->
                            clause.columns(j.id, j.worldId, j.amount).values(id, worldId, 1).execute()
                        }, { j, clause ->
                            clause.where(j.id.eq(id), j.worldId.eq(worldId)).set(j.amount, j.amount.add(1)).execute()
                        },
                        connection, plugin
                    )
                }
            }

            plugin.threadManager.scheduleRaw(QSeen::class.java) { connection ->
                QSeen.seen.runQuery(
                    { s, clause ->
                        clause.columns(s.id, s.lastJoinTime).values(id, currentTime).execute()
                    }, { s, clause ->
                        clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute()
                    },
                    connection, plugin
                )
            }
        }

        ServerStatUpdater.Move(plugin).run()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onLeave(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

        plugin.threadManager.schedule<QSeen>(
            uuid, worldName,
            { s, clause, id, worldId ->
                clause.columns(s.id, s.lastLeaveTime).values(id, currentTime).execute()
            }, { s, clause, id, worldId ->
                clause.where(s.id.eq(id)).set(s.lastLeaveTime, currentTime).execute()
            }
        )

        val currentPlayTime = Math.round(event.player.getStatistic(Statistic.PLAY_ONE_TICK) * 0.052).toInt()

        plugin.threadManager.schedule<QPlayTime>(
            uuid, worldName,
            { p, clause, id, worldId ->
                clause.columns(p.id, p.amount).values(id, currentPlayTime).execute()
            }, { p, clause, id, worldId ->
                clause.where(p.id.eq(id)).set(p.amount, currentPlayTime).execute()
            }
        )

        plugin.moveUpdater.run(event.player)
    }
}
