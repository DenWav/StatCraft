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
import com.demonwav.statcraft.querydsl.QPlayTime
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QSeen
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCPlayTime(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("playtime", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.playtime")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Play Time" }
            stats["Total"] = "0"
        }

        var query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QPlayTime.playTime
        var result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount.sum()) ?: 0

        val uuid = plugin.players[name]
        val player = plugin.server.getOfflinePlayer(uuid)

        if (player != null && player.isOnline) {
            val now = (System.currentTimeMillis() / 1000L).toInt()

            val s = QSeen.seen
            query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            val join = query.from(s).where(s.id.eq(id)).uniqueResult(s.lastJoinTime.max())

            if (join != null && join != 0 && now != 0) {
                result += now - join
            }
        }

        return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Play Time" }
            stats["Total"] = result.toString()
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QPlayTime.playTime
        val p = QPlayers.players

        val list = query
            .from(t)
            .leftJoin(p)
            .on(t.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(t.amount.sum().desc())
            .limit(num)
            .list(p.name, t.amount.sum())

        return topListResponse("Play Time", list)
    }
}
