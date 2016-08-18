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
import com.demonwav.statcraft.magic.BucketCode
import com.demonwav.statcraft.querydsl.QBucketEmpty
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCBucketsEmptied(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("bucketsemptied", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.bucketsemptied")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        try {
            val id = getId(name) ?: throw Exception()

            val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            val e = QBucketEmpty.bucketEmpty
            val results = query.from(e).where(e.id.eq(id)).list(e)

            var water = 0
            var lava = 0
            var milk = 0

            for (bucketEmpty in results) {
                val code = BucketCode.fromCode(bucketEmpty.type) ?: continue

                when (code) {
                    BucketCode.WATER -> water += bucketEmpty.amount
                    BucketCode.LAVA -> lava += bucketEmpty.amount
                    BucketCode.MILK -> milk += bucketEmpty.amount
                }
            }

            val total = water + lava + milk

            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Buckets Emptied" }
                stats["Total"] = df.format(total)
                stats["Water"] = df.format(water)
                stats["Lava"] = df.format(lava)
                stats["Milk"] = df.format(milk)
            }
        } catch (e: Exception) {
            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Buckets Emptied" }
                stats["Total"] = 0.toString()
                stats["Water"] = 0.toString()
                stats["Lava"] = 0.toString()
                stats["Milk"] = 0.toString()
            }
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val e = QBucketEmpty.bucketEmpty
        val p = QPlayers.players

        val result = query
            .from(e)
            .leftJoin(p)
            .on(e.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(e.amount.sum().desc())
            .limit(num)
            .list(p.name, e.amount.sum())

        return topListResponse("Buckets Emptied", result)
    }
}
