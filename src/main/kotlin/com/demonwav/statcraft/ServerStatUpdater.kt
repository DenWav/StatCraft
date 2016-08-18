/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import com.demonwav.statcraft.magic.MoveCode
import com.demonwav.statcraft.querydsl.QMove
import org.bukkit.entity.Player

class ServerStatUpdater {

    class Move(private val plugin: StatCraft) : Runnable {
        override fun run() {
            plugin.server.onlinePlayers.forEach { run(it) }
        }

        fun run(player: Player) {
            run(player, player.world.name)
        }

        fun run(player: Player, worldName: String) {
            MoveCode.values().forEach { code ->
                val stat = code.stat
                val value = player.getStatistic(stat)
                val uuid = player.uniqueId

                plugin.threadManager.schedule<QMove, Int>(
                    uuid, worldName,
                    { m, query, id, worldId ->
                        query.from(m).where(m.id.eq(id), m.vehicle.eq(code.code)).uniqueResult(m.distance.sum()) ?: 0
                    },
                    { m, clause, id, worldId, currentTotal ->
                        clause.columns(m.id, m.worldId, m.vehicle, m.distance)
                            .values(id, worldId, code.code, value - currentTotal).execute()
                    },
                    { m, clause, id, worldId, currentTotal ->
                        clause.where(m.id.eq(id), m.worldId.eq(worldId), m.vehicle.eq(code.code))
                            .set(m.distance, m.distance.add(value - currentTotal)).execute()
                    }
                )
            }
        }
    }
}
