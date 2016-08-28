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
import com.demonwav.statcraft.querydsl.QWordFrequency
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCWordsSpoken(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("wordsspoken", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.wordsspoken")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Words Spoken" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val m = QMessagesSpoken.messagesSpoken
        val result = query.from(m).where(m.id.eq(id)).uniqueResult(m.wordsSpoken.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Words Spoken" }
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
            .orderBy(m.amount.desc())
            .limit(num)
            .list(p.name, m.amount)

        return topListResponse("Words Spoken", list)
    }
}
