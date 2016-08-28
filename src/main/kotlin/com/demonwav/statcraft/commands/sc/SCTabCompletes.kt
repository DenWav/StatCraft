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
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QTabComplete
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCTabCompletes(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("tabcompletes", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.tabcompletes")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Tab Completes" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QTabComplete.tabComplete
        val result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount.sum()) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Tab Completes" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QTabComplete.tabComplete
        val p = QPlayers.players

        val list = query
            .from(t)
            .leftJoin(p)
            .on(t.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(t.amount.desc())
            .limit(num)
            .list(p.name, t.amount)

        return topListResponse("Tab Completes", list)
    }
}
