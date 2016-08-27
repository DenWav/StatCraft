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
import com.demonwav.statcraft.magic.MoveCode
import com.demonwav.statcraft.querydsl.QMove
import com.demonwav.statcraft.querydsl.QPlayers
import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.util.ArrayList

class SCMove(plugin: StatCraft) : SCTemplate(plugin) {

    private val tabComplete = ArrayList<String>(15)

    init {
        plugin.baseCommand.registerCommand("move", this)

        tabComplete.add("-all")
        tabComplete.add("-boat")
        tabComplete.add("-breakdown")
        tabComplete.add("-climbing")
        tabComplete.add("-crouching")
        tabComplete.add("-diving")
        tabComplete.add("-elytra")
        tabComplete.add("-falling")
        tabComplete.add("-flying")
        tabComplete.add("-horse")
        tabComplete.add("-minecart")
        tabComplete.add("-pig")
        tabComplete.add("-sprinting")
        tabComplete.add("-swimming")
        tabComplete.add("-walking")
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.move")

    @SecondaryArgument("walking", "crouching", "sprinting", "swimming", "falling", "climbing", "flying", "diving", "minecart", "boat",
        "pig", "horse", "breakdown", "elytra")
    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        var arg: String? = null
        try {
            val id = getId(name) ?: throw Exception()

            val m = QMove.move
            val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

            if (args.size == 0) {
                val result = query.from(m).where(m.id.eq(id)).uniqueResult(m.distance.sum()) ?: throw Exception()

                return ResponseBuilderKt.build(plugin) {
                    playerName { name }
                    statName { "Move" }
                    stats["Total"] = Util.distanceUnits(result)
                }
            } else {
                arg = args[0]
                if (arg.equals("breakdown", false)) {
                    val list = query
                        .from(m)
                        .where(m.id.eq(id))
                        .groupBy(m.vehicle)
                        .orderBy(m.distance.sum().desc())
                        .list(m.vehicle, m.distance.sum())

                    val sb = StringBuilder()

                    sb  .append(ChatColor.valueOf(plugin.config.colors.statTitle))
                        .append("- ")
                        .append(ChatColor.valueOf(plugin.config.colors.playerName))
                        .append(name).append(" ")
                        .append(ChatColor.valueOf(plugin.config.colors.statSeparator))
                        .append("| ")
                        .append(ChatColor.valueOf(plugin.config.colors.statTitle))
                        .append("Move Breakdown")
                        .append(" -")

                    for (tuple in list) {
                        val vehicle = tuple[m.vehicle] ?: continue
                        val code = MoveCode.fromCode(vehicle) ?: continue
                        val distance = tuple[m.distance.sum()] ?: continue

                        sb  .append("\n")
                            .append(ChatColor.valueOf(plugin.config.colors.statLabel))
                            .append(WordUtils.capitalizeFully(code.name))
                            .append(": ")
                            .append(ChatColor.valueOf(plugin.config.colors.statValue))
                            .append(Util.distanceUnits(distance))

                    }

                    return sb.toString()
                } else {
                    val code = MoveCode.valueOf(arg.toUpperCase())
                    val result = query
                        .from(m)
                        .where(m.id.eq(id), m.vehicle.eq(code.code))
                        .uniqueResult(m.distance.sum()) ?: throw Exception()

                    return ResponseBuilderKt.build(plugin) {
                        playerName { name }
                        statName { "Move" }
                        stats[WordUtils.capitalizeFully(arg)] = Util.distanceUnits(result)
                    }
                }
            }
        } catch (e: Exception) {
            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Move" }
                stats[if (arg == null) "Total" else WordUtils.capitalizeFully(arg)] = Util.distanceUnits(0)
            }
        }
    }

    @SecondaryArgument("walking", "crouching", "sprinting", "swimming", "falling", "climbing", "flying", "diving", "minecart", "boat",
        "pig", "horse", "elytra")
    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val m = QMove.move
        val p = QPlayers.players

        var arg: String? = null
        val list = if (args.size == 0) {
            query
                .from(m)
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum())
        } else {
            arg = args[0]
            val code = MoveCode.valueOf(arg.toUpperCase())
            query
                .from(m)
                .where(m.vehicle.eq(code.code))
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum())
        }

        return topListDistanceResponse(if (arg == null) "Move" else WordUtils.capitalizeFully(arg), list)
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args[args.size - 1].startsWith("-")) {
            return tabComplete.filter { it.startsWith(args[args.size - 1]) }
        } else {
            return super.onTabComplete(sender, args)
        }
    }
}
