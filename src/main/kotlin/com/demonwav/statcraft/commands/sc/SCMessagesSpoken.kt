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
import com.demonwav.statcraft.querydsl.QMessagesSpoken
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCMessagesSpoken(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("messagesspoken", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.messgesspoken")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Messages Spoken" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val m = QMessagesSpoken.messagesSpoken
        val result = query.from(m).where(m.id.eq(id)).uniqueResult(m.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Messages Spoken" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val m = QMessagesSpoken.messagesSpoken
        val p = QPlayers.players

        val list = query
            .from(m)
            .innerJoin(p)
            .on(m.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(m.amount.sum().desc())
            .limit(num)
            .list(p.name, m.amount.sum())

        return topListResponse("Messages Spoken", list)
    }
}
