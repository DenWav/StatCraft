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
import com.demonwav.statcraft.commands.ResponseBuilderKt
import com.demonwav.statcraft.querydsl.QJoins
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCJoins(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("joins", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.joins")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Joins" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val j = QJoins.joins

        val result = query.from(j).where(j.id.eq(id)).uniqueResult(j.amount.sum()) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Joins" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val j = QJoins.joins
        val p = QPlayers.players

        val list = query
            .from(j)
            .leftJoin(p)
            .groupBy(p.name)
            .orderBy(j.amount.desc())
            .limit(num)
            .list(p.name, j.amount)

        return topListResponse("Joins", list)
    }
}
