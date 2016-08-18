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

class SCEggsThrown(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("eggsthrown", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.eggsthrown")

    @SecondaryArgument("distance", "farthest")
    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val total: Int
        var hatched = 0
        var notHatched = 0

        var unHatchedDistance = 0
        var hatchedDistance = 0

        var unHatchedMaxThrow = 0
        var hatchedMaxThrow = 0

        try {
            val id = getId(name) ?: throw Exception()

            val p = QProjectiles.projectiles
            val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            val list = query
                .from(p)
                .where(
                    p.id.eq(id),
                    p.type.eq(ProjectilesCode.HATCHED_EGG.code).or(p.type.eq(ProjectilesCode.UNHATCHED_EGG.code))
                ).list(p)

            for (projectiles in list) {
                val code = ProjectilesCode.fromCode(projectiles.type) ?: continue

                when (code) {
                    ProjectilesCode.HATCHED_EGG -> {
                        hatched += projectiles.amount
                        hatchedDistance += projectiles.totalDistance
                        hatchedMaxThrow = Math.max(hatchedMaxThrow, projectiles.maxThrow)
                    }
                    ProjectilesCode.UNHATCHED_EGG -> {
                        notHatched += projectiles.totalDistance
                        unHatchedDistance += projectiles.totalDistance
                        unHatchedMaxThrow += Math.max(unHatchedMaxThrow, projectiles.maxThrow)
                    }
                    else -> {}
                }
            }

            total = hatched + notHatched
        } catch (e: Exception) {
            total = 0
            hatched = 0
            notHatched = 0
            unHatchedDistance = 0
            hatchedDistance = 0
            unHatchedMaxThrow = 0
            hatchedMaxThrow = 0
        }

        val arg = if (args.size > 0) {
            args[0]
        } else {
            ""
        }

        when (arg) {
            "distance" -> return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Eggs Thrown Total Distance" }
                stats["Total"] = Util.distanceUnits(hatchedDistance + unHatchedDistance)
                stats["Hatched"] = Util.distanceUnits(hatchedDistance)
                stats["Not Hatched"] = Util.distanceUnits(unHatchedDistance)
            }
            "farthest" -> return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Eggs Thrown Farthest Distance" }
                stats["Hatched"] = Util.distanceUnits(hatchedMaxThrow)
                stats["Not Hatched"] = Util.distanceUnits(unHatchedMaxThrow)
            }
            else -> return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Eggs Thrown" }
                stats["Total"] = df.format(total)
                stats["Hatched"] = df.format(hatched)
                stats["Not Hatched"] = df.format(notHatched)
            }
        }
    }

    @SecondaryArgument("distance", "farthest", "unhatched")
    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        var distance = false

        val p = QProjectiles.projectiles
        val pl = QPlayers.players
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        var path: NumberPath<Int>? = null
        var titlePrefix = ""
        var titlePostfix = ""

        var code = ProjectilesCode.HATCHED_EGG

        for (arg in args) {
            when (arg) {
                "distance" -> {
                    path = p.totalDistance
                    titlePrefix = "Total Distance Thrown - "
                    distance = true
                }
                "farthest" -> {
                    path = p.maxThrow
                    titlePrefix = "Farthest "
                    titlePostfix = " Thrown"
                    distance = true
                }
                "unhatched" -> {
                    code = ProjectilesCode.UNHATCHED_EGG
                }
            }
        }

        if (path == null) {
            path = p.amount
            titlePostfix = "s Thrown"
        }

        val title = titlePrefix + (if (code == ProjectilesCode.HATCHED_EGG) "Hatched Egg" else "Not Hatched Egg") + titlePostfix

        val list = query
            .from(p)
            .leftJoin(pl)
            .on(p.id.eq(pl.id))
            .where(p.type.eq(code.code))
            .groupBy(pl.name)
            .orderBy(path?.desc())
            .limit(num)
            .list(pl.name, path?.sum())

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

            val list = LinkedList<String>()
            list.add("-all")
            list.add("-distance")
            list.add("-farthest")
            if (top) {
                list.add("-unhatched")
            }

            return list.filter { it.startsWith(args[args.size - 1]) }
        } else {
            return super.onTabComplete(sender, args)
        }
    }
}
