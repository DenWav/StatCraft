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

class SCSnowballs(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("snowballs", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.snowball")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return emptyResponse(name)

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val p = QProjectiles.projectiles

        val tuple = query.from(p).where(p.id.eq(id), p.type.eq(ProjectilesCode.SNOWBALL.code))
            .uniqueResult(p.amount.sum(), p.totalDistance.sum(), p.maxThrow.max()) ?: return emptyResponse(name)

        val amount = tuple[p.amount] ?: 0
        val distance = tuple[p.totalDistance] ?: 0
        val maxThrow = tuple[p.maxThrow] ?: 0

        return ResponseBuilderKt.build(plugin) {
            playerName { name }
            statName { "Snowballs Thrown" }
            stats["Thrown"] = df.format(amount)
            stats["Distance"] = Util.distanceUnits(distance)
            stats["Farthest Thrown"] = Util.distanceUnits(maxThrow)
        }
    }

    @SecondaryArgument("distance", "farthest")
    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        var distance = false

        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val p = QProjectiles.projectiles
        val pl = QPlayers.players

        var path: NumberPath<Int>
        var title: String

        val arg = if (args.size > 0) {
            args[0]
        } else {
            ""
        }

        when (arg) {
            "distance" -> {
                path = p.totalDistance
                title = "Total Snowballs Thrown Distance"
                distance = true
            }
            "farthest" -> {
                path = p.maxThrow
                title = "Farthest Snowball Thrown"
                distance = true
            }
            else -> {
                path = p.amount
                title = "Snowballs Thrown"
            }
        }

        val list = query
            .from(p)
            .leftJoin(pl)
            .on(p.id.eq(pl.id))
            .where(p.type.eq(ProjectilesCode.SNOWBALL.code))
            .groupBy(pl.name)
            .orderBy(path.desc())
            .limit(num)
            .list(pl.name, path)

        if (distance) {
            return topListDistanceResponse(title, list)
        } else {
            return topListResponse(title, list)
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

    private fun emptyResponse(name: String) = ResponseBuilderKt.build(plugin) {
        playerName { name }
        statName { "Snowballs Thrown" }
        stats["Thrown"] = "0"
        stats["Distance"] = Util.distanceUnits(0)
        stats["Farthest Throw"] = Util.distanceUnits(0)
    }
}
