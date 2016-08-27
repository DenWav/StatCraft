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
import com.demonwav.statcraft.commands.TimeResponseBuilderKt
import com.demonwav.statcraft.querydsl.QOnFire
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCOnFire(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("onfire", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.onfire")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "On Fire" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val o = QOnFire.onFire
        val result = query.from(o).where(o.id.eq(id)).uniqueResult(o.time.sum()) ?: 0

        return TimeResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "On Fire" }
            stats["Total"] = result.toString()
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val o = QOnFire.onFire
        val p = QPlayers.players

        val list = query
            .from(o)
            .leftJoin(p)
            .on(o.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(o.time.sum().desc())
            .limit(num)
            .list(p.name, o.time.sum())

        return topListTimeResponse("On Fire", list)
    }
}
