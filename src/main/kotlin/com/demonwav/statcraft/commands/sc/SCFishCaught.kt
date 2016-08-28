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
import com.demonwav.statcraft.magic.FishCode
import com.demonwav.statcraft.querydsl.QFishCaught
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCFishCaught(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("fishcaught", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.fishcaught")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Fish Caught" }
            stats["Total"] = "0"
            stats["Fish"] = "0"
            stats["Treasure"] = "0"
            stats["Junk"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val f = QFishCaught.fishCaught
        val list = query.from(f).where(f.id.eq(id)).groupBy(f.type).list(f.type, f.amount.sum())

        var fish = 0
        var treasure = 0
        var junk = 0

        for (tuple in list) {
            val type = tuple.get(f.type) ?: continue

            val code = FishCode.fromCode(type) ?: continue

            val sum = tuple.get(f.amount.sum()) ?: 0

            when (code) {
                FishCode.FISH -> fish = sum
                FishCode.TREASURE -> treasure = sum
                FishCode.JUNK -> junk = sum
            }
        }

        val total = fish + treasure + junk

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Fish Caught" }
            stats["Total"] = df.format(total)
            stats["Fish"] = df.format(fish)
            stats["Treasure"] = df.format(treasure)
            stats["Junk"] = df.format(junk)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val f = QFishCaught.fishCaught
        val p = QPlayers.players

        val list = query
            .from(f)
            .innerJoin(p)
            .on(f.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(f.amount.sum().desc())
            .limit(num)
            .list(p.name, f.amount.sum())

        return topListResponse("Fish Caught", list)
    }
}
