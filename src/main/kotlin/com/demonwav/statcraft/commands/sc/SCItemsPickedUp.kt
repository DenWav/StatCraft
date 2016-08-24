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
import com.demonwav.statcraft.querydsl.QItemPickups
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCItemsPickedUp(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("itemspickedup", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.itemspickedup")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Items Picked Up" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val i = QItemPickups.itemPickups
        val result = query.from(i).where(i.id.eq(id)).uniqueResult(i.amount.sum()) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Items Picked Up" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val i = QItemPickups.itemPickups
        val p = QPlayers.players

        val list = query
            .from(i)
            .leftJoin(p)
            .on(i.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(i.amount.sum().desc())
            .limit(num)
            .list(p.name, i.amount.sum())

        return topListResponse("Items Picked Up", list)
    }
}
