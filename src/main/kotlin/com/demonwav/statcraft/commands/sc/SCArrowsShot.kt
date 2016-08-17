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

class SCArrowsShot(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("arrowsshot", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<String>?) = sender.hasPermission("statcraft.user.arrowsshot")

    @SecondaryArgument("distance", "farthest")
    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val total: Int
        var normal = 0
        var flaming = 0

        var normalDistance = 0
        var flamingDistance = 0

        var normalMaxThrow = 0
        var flamingMaxThrow = 0

        try {
            val id = getId(name)
            if (id < 0) {
                throw Exception()
            }

            val query = plugin.databaseManager.getNewQuery(connection) ?:
                return "Sorry, there seems to be an issue connecting to the database right now"

            val p = QProjectiles.projectiles
            val result = query.from(p).where(
                p.id.eq(id),
                p.type.eq(ProjectilesCode.NORMAL_ARROW.code).or(p.type.eq(ProjectilesCode.FLAMING_ARROW.code))
            ).list(p)

            result.forEach { projectiles ->
                val code = ProjectilesCode.fromCode(projectiles.type) ?: return@forEach

                when (code) {
                    ProjectilesCode.NORMAL_ARROW -> {
                        normal += projectiles.amount
                        normalDistance += projectiles.totalDistance
                        normalMaxThrow = Math.max(normalMaxThrow, projectiles.maxThrow)
                    }
                    ProjectilesCode.FLAMING_ARROW -> {
                        flaming += projectiles.amount
                        flamingDistance += projectiles.totalDistance
                        flamingMaxThrow = Math.max(flamingMaxThrow, projectiles.maxThrow)
                    }
                    else -> {}
                }
            }

            total = normal + flaming
        } catch (e: Exception) {
            total = 0
            normal = 0
            flaming = 0
            normalDistance = 0
            flamingDistance = 0
            normalMaxThrow = 0
            flamingMaxThrow = 0
        }

        val arg: String
        if (args.size > 0) {
            arg = args[0]
        } else {
            arg = ""
        }

        when (arg) {
            "distance" -> {
                return ResponseBuilderKt.build(plugin) {
                    playerName { name }
                    statName { "Arrows Shot Total Distance" }
                    stats["Total"] = Util.distanceUnits(normalDistance + flamingDistance)
                    stats["Normal"] = Util.distanceUnits(normalDistance)
                    stats["Flaming"] = Util.distanceUnits(flamingDistance)
                }
            }
            "farthest" -> {
                return ResponseBuilderKt.build(plugin) {
                    playerName { name }
                    statName { "Farthest Arrows Shot" }
                    stats["Normal"] = Util.distanceUnits(normalMaxThrow)
                    stats["Flaming"] = Util.distanceUnits(flamingMaxThrow)
                }
            }
            else -> {
                return ResponseBuilderKt.build(plugin) {
                    playerName { name }
                    statName { "Arrows Shot" }
                    stats["Total"] = df.format(total)
                    stats["Normal"] = df.format(normal)
                    stats["Flaming"] = df.format(flaming)
                }
            }
        }
    }

    @SecondaryArgument("distance", "farthest", "flaming")
    override fun serverStatListResponse(num: Int, args: List<String>, connection: Connection): String {
        var distance = false
        val p = QProjectiles.projectiles
        val pl = QPlayers.players
        val query = plugin.databaseManager.getNewQuery(connection) ?:
            return "Sorry, there seems to be an issue connecting to the database right now."

        var path: NumberPath<Int>? = null
        var titlePrefix = ""
        var titlePostfix = ""
        val title: String

        var code = ProjectilesCode.NORMAL_ARROW

        for (arg in args) {
            when (arg) {
                "distance" -> {
                    path = p.totalDistance
                    titlePrefix = "Total Distance Fired - "
                    distance = true
                }
                "farthest" -> {
                    path = p.maxThrow
                    titlePrefix = "Farthest "
                    titlePostfix = " Shot"
                    distance = true
                }
                "flaming" -> code = ProjectilesCode.FLAMING_ARROW
            }
        }

        if (path == null) {
            path = p.amount
            titlePostfix = "s Shot"
        }

        title = titlePrefix + (if (code == ProjectilesCode.NORMAL_ARROW) "Normal Arrow" else "Flaming Arrow") + titlePostfix

        val list = query
            .from(p)
            .leftJoin(pl)
            .on(p.id.eq(pl.id))
            .where(p.type.eq(code.code))
            .groupBy(pl.name)
            .orderBy(path!!.desc())
            .limit(num.toLong())
            .list(pl.name, path.sum())

        if (distance) {
            return topListDistanceResponse(title, list)
        } else {
            return topListResponse(title, list)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args[args.size - 1].startsWith("-")) {
            var top = false
            for (s in args) {
                if (s.startsWith("-top")) {
                    top = true
                }
            }
            val list = LinkedList<String>()
            list.add("-all")
            list.add("-distance")
            list.add("-farthest")
            if (top) {
                list.add("-flaming")
            }

            return list.filter({ s -> s.startsWith(args[args.size - 1]) }).toList()
        } else {
            return super.onTabComplete(sender, args)
        }
    }
}
