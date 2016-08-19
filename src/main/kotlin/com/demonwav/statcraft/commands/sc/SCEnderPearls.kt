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
import com.demonwav.statcraft.Util
import com.demonwav.statcraft.commands.ResponseBuilderKt
import com.demonwav.statcraft.commands.SecondaryArgument
import com.demonwav.statcraft.magic.ProjectilesCode
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QProjectiles
import com.mysema.query.types.path.NumberPath
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.util.LinkedList

class SCEnderPearls(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("enderpearls", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.enderpearls")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        try {
            val id = getId(name) ?: throw Exception()

            val p = QProjectiles.projectiles
            val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            val tuple = query.from(p).where(p.id.eq(id), p.type.eq(ProjectilesCode.ENDER_PEARL.code))
                .uniqueResult(p.amount.sum(), p.totalDistance.sum(), p.maxThrow.max()) ?: throw Exception()

            val amount = tuple.get(p.amount) ?: 0
            val distance = tuple.get(p.totalDistance) ?: 0
            val maxThrow = tuple.get(p.maxThrow) ?: 0

            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Ender Pearls Thrown" }
                stats["Total"] = df.format(amount)
                stats["Distance"] = Util.distanceUnits(distance)
                stats["Farthest Throw"] = Util.distanceUnits(maxThrow)
            }
        } catch (e: Exception) {
            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Ender Pearls Thrown" }
                stats["Thrown"] = 0.toString()
                stats["Distance"] = 0.toString()
                stats["Farthest Throw"] = 0.toString()
            }
        }
    }

    @SecondaryArgument("distance", "farthest")
    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        var distance = false
        val p = QProjectiles.projectiles
        val pl = QPlayers.players

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val path: NumberPath<Int>
        val title: String

        val arg = if (args.size > 0) {
            args[0]
        } else {
            ""
        }

        when (arg) {
            "distance" -> {
                path = p.totalDistance
                title = "Distance Traveled With Ender Pearls"
                distance = true
            }
            "farthest" -> {
                path = p.maxThrow
                title = "Farthest Ender Pearl Throw"
                distance = true
            }
            else -> {
                path = p.amount
                title = "Ender Pearls Thrown"
            }
        }

        val list = query
            .from(p)
            .leftJoin(pl)
            .on(p.id.eq(pl.id))
            .where(p.type.eq(ProjectilesCode.ENDER_PEARL.code))
            .groupBy(pl.name)
            .orderBy(path.desc())
            .limit(num)
            .list(pl.name, path)

        return if (distance) {
            topListDistanceResponse(title, list)
        } else {
            topListResponse(title, list)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args[args.size - 1].startsWith("-")) {
            var top = false
            for (s in args) {
                if (s.startsWith("-top")) {
                    top = true
                }
            }

            if (top) {
                val list = LinkedList<String>()
                list.add("-all")
                list.add("-distance")
                list.add("-farthest")

                return list.filter { it.startsWith(args[args.size - 1]) }
            } else {
                return super.onTabComplete(sender, args)
            }
        } else {
            return super.onTabComplete(sender, args)
        }
    }
}
