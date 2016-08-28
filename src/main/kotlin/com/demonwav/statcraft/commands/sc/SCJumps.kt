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
import com.demonwav.statcraft.querydsl.QJumps
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCJumps(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("jumps", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.jumps")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Jumps" }
            stats["Total"] = "0"
        }

        val j = QJumps.jumps
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val result = query.from(j).where(j.id.eq(id)).uniqueResult(j.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Jumps" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val j = QJumps.jumps
        val p = QPlayers.players

        val list = query
            .from(j)
            .innerJoin(p)
            .on(j.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(j.amount.sum().desc())
            .limit(num)
            .list(p.name, j.amount.sum())

        return topListResponse("Jumps", list)
    }
}
