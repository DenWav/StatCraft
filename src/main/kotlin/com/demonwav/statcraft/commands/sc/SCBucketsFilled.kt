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
import com.demonwav.statcraft.magic.BucketCode
import com.demonwav.statcraft.querydsl.QBucketFill
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCBucketsFilled(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("bucketsfilled", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.bucketsfilled")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Buckets Filled" }
            stats["Total"] = "0"
            stats["Water"] = "0"
            stats["Lava"] = "0"
            stats["Milk"] = "0"
        }

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val f = QBucketFill.bucketFill

        val results = query.from(f).where(f.id.eq(id)).list(f)

        var water = 0
        var lava = 0
        var milk = 0

        for (bucketFill in results) {
            val code = BucketCode.fromCode(bucketFill.type) ?: continue

            when (code) {
                BucketCode.WATER -> water += bucketFill.amount
                BucketCode.LAVA -> lava += bucketFill.amount
                BucketCode.MILK -> milk += bucketFill.amount
            }
        }
        val total = water + lava + milk

        return ResponseBuilder.build(plugin) {
            playerName { name }
            statName { "Buckets Filled" }
            stats["Total"] = df.format(total)
            stats["Water"] = df.format(water)
            stats["Lava"] = df.format(lava)
            stats["Milk"] = df.format(milk)
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val f = QBucketFill.bucketFill
        val p = QPlayers.players

        val result = query
            .from(f)
            .innerJoin(p)
            .on(f.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(f.amount.sum().desc())
            .limit(num)
            .list(p.name, f.amount.sum())

        return topListResponse("Buckets Filled", result)
    }
}
