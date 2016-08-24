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
import com.demonwav.statcraft.querydsl.QBlockBreak
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCBlocksBroken(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("blocksbroken", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.blocksbroken")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "BlocksBroken" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val b = QBlockBreak.blockBreak
        val total = query.from(b).where(b.id.eq(id)).uniqueResult(b.amount.sum()) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "BlocksBroken" }
            stats["Total"] = df.format(total)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val b = QBlockBreak.blockBreak
        val p = QPlayers.players

        val result = query
            .from(b)
            .leftJoin(p)
            .on(b.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(b.amount.sum().desc())
            .limit(num)
            .list(p.name, b.amount.sum())

        return topListResponse("Blocks Broken", result)
    }
}
