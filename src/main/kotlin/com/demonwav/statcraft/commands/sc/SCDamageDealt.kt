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
import com.demonwav.statcraft.querydsl.QDamageDealt
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCDamageDealt(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("damagedealt", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.damagedealt")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Damage Dealt" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val d = QDamageDealt.damageDealt
        val total = query.from(d).where(d.id.eq(id)).uniqueResult(d.amount.sum()) ?: 0

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Damage Dealt" }
            stats["Total"] = df.format(total)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val d = QDamageDealt.damageDealt
        val p = QPlayers.players

        val list = query
            .from(d)
            .innerJoin(p)
            .on(d.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(d.amount.sum().desc())
            .limit(num)
            .list(p.name, d.amount.sum())

        return topListResponse("Damage Dealt", list)
    }
}
