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
import com.demonwav.statcraft.querydsl.QDeath
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCDeaths(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("deaths", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.deaths")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Deaths" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection)  ?: return databaseError
        val d = QDeath.death

        val total = query.from(d).where(d.id.eq(id)).uniqueResult(d.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Deaths" }
            stats["Total"] = df.format(total)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val d = QDeath.death
        val p = QPlayers.players

        val list = query
            .from(d)
            .innerJoin(p)
            .on(d.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(d.amount.sum().desc())
            .limit(num)
            .list(p.name, d.amount.sum())

        return topListResponse("Deaths", list)
    }
}
