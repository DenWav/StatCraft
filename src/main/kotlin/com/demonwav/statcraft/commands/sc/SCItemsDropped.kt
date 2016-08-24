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
import com.demonwav.statcraft.querydsl.QItemDrops
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCItemsDropped(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("itemsdropped", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.itemsdropped")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Items Dropped" }
            stats["Total"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val i = QItemDrops.itemDrops
        val result = query.from(i).where(i.id.eq(id)).uniqueResult(i.amount.sum()) ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Items Dropped" }
            stats["Total"] = df.format(result)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val i = QItemDrops.itemDrops
        val p = QPlayers.players

        val list = query
            .from(i)
            .leftJoin(p)
            .on(i.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(i.amount.sum().desc())
            .limit(num)
            .list(p.name, i.amount.sum())

        return topListResponse("Items Dropped", list)
    }
}
