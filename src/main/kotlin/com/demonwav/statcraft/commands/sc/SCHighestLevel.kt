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
import com.demonwav.statcraft.querydsl.QHighestLevel
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCHighestLevel(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("highestlevel", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.highestlevel")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Highest Level" }
            stats["Level"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val h = QHighestLevel.highestLevel
        val result = query.from(h).where(h.id.eq(id)).uniqueResult(h.level) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Highest Level" }
            stats["Level"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val h = QHighestLevel.highestLevel
        val p = QPlayers.players

        val list = query
            .from(h)
            .leftJoin(p)
            .on(h.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(h.level.desc())
            .limit(num)
            .list(p.name, h.level)

        return topListResponse("Highest Level", list)
    }
}
