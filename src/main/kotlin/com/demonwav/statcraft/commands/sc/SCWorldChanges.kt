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
import com.demonwav.statcraft.querydsl.QWorldChange
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCWorldChanges(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("worldchange", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.worldchanges")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "World Changes" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val w = QWorldChange.worldChange
        val result = query.from(w).where(w.id.eq(id)).uniqueResult(w.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "World Changes" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val w = QWorldChange.worldChange
        val p = QPlayers.players

        val list = query
            .from(w)
            .innerJoin(p)
            .on(w.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(w.amount.sum().desc())
            .limit(num)
            .list(p.name, w.amount.sum())

        return topListResponse("World Changes", list)
    }
}
