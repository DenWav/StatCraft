/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands.sc

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.commands.TimeResponseBuilderKt
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QSleep
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCTimeSlept(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("timeslept", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.bed")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Time Slept" }
            stats["Total"] = "0"
        }

        var query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val s = QSleep.sleep
        var result = query.from(s).where(s.id.eq(id)).uniqueResult(s.timeSlept) ?: 0

        val uuid = plugin.players[name]
        val player = plugin.server.getOfflinePlayer(uuid)

        if (player.isOnline && player.player.isSleeping) {
            val now = (System.currentTimeMillis() / 1000L).toInt()

            query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            val enter = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed)

            // Sanity check
            if (enter != null && enter != 0 && now != 0) {
                result += now - enter
            }
        }

        return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Time Slept" }
            stats["Total"] = result.toString()
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val s = QSleep.sleep
        val p = QPlayers.players

        val list = query
            .from(s)
            .leftJoin(p)
            .on(s.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(s.timeSlept.sum().desc())
            .limit(num)
            .list(p.name, s.timeSlept.sum())

        return topListTimeResponse("Time Slept", list)
    }
}
