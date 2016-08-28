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
import com.demonwav.statcraft.commands.ResponseBuilder
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QXpGained
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCXpGained(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("xpgained", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.xpgained")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Xp Gained" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val x = QXpGained.xpGained
        val result = query.from(x).where(x.id.eq(id)).uniqueResult(x.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Xp Gained" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val x = QXpGained.xpGained
        val p = QPlayers.players

        val list = query
            .from(x)
            .innerJoin(p)
            .on(x.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(x.amount.sum().desc())
            .limit(num)
            .list(p.name, x.amount.sum())

        return topListResponse("Xp Gained", list)
    }
}
