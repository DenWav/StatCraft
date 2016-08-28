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
import com.demonwav.statcraft.querydsl.QToolsBroken
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCToolsBroken(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("toolsbroken", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.toolsbroken")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Tools Broken" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QToolsBroken.toolsBroken
        val result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount.sum())

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Tools Broken" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val t = QToolsBroken.toolsBroken
        val p = QPlayers.players

        val list = query
            .from(t)
            .innerJoin(p)
            .on(t.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(t.amount.sum().desc())
            .limit(num)
            .list(p.name, t.amount.sum())

        return topListResponse("Tools Broken", list)
    }
}
