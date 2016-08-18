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
import com.mysema.query.Tuple
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.text.DecimalFormat

abstract class SCTemplate(@JvmField protected val plugin: StatCraft) {

    @JvmField
    protected val df = DecimalFormat("#,###")
    protected val databaseError = "Sorry, there seems to be an issue connecting to the database right now."

    open fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        val word = args[args.size - 1]
        val result = plugin.players.keys.filter { it.toLowerCase().startsWith(word.toLowerCase()) }
        return result.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    abstract fun hasPermission(sender: CommandSender, args: Array<out String>?): Boolean

    abstract fun playerStatResponse(name: String, args: List<String>, connection: Connection): String

    abstract fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String?

    protected fun topListResponse(name: String, list: List<Tuple>): String {
        val sb = StringBuilder()

        sb  .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("- ").append(name).append(" ")
            .append(ChatColor.valueOf(plugin.config.colors.statSeparator))
            .append("| ")
            .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("Top ")
            .append(list.size)
            .append(" -")

        var i = 0

        for (tuple in list) {
            sb  .append('\n')
                .append(ChatColor.RESET)
                .append(ChatColor.BOLD)
                .append(ChatColor.valueOf(plugin.config.colors.listNumber))
                .append(++i)
                .append(". ")
                .append(ChatColor.RESET)
                .append(ChatColor.valueOf(plugin.config.colors.playerName))
                .append(tuple.get(0, String::class.java))
                .append(ChatColor.WHITE)
                .append(": ")
                .append(ChatColor.valueOf(plugin.config.colors.statValue))
                .append(df.format(tuple.get(1, Int::class.java)))
                .append(ChatColor.RESET)
        }

        return sb.toString()
    }

    protected fun topListTimeResponse(name: String, list: List<Tuple>): String {
        val sb = StringBuilder()

        sb  .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("- ")
            .append(name)
            .append(" ")
            .append(ChatColor.valueOf(plugin.config.colors.statSeparator))
            .append("| ")
            .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("Top ")
            .append(list.size)
            .append(" -")

        var i = 0

        for (tuple in list) {
            val res = tuple.get(1, Int::class.java)

            sb  .append('\n')
                .append(ChatColor.RESET)
                .append(ChatColor.BOLD)
                .append(ChatColor.valueOf(plugin.config.colors.listNumber))
                .append(++i)
                .append(". ")
                .append(ChatColor.RESET)
                .append(ChatColor.valueOf(plugin.config.colors.playerName))
                .append(tuple.get(0, String::class.java))
                .append(ChatColor.WHITE)
                .append(": ")
                .append(ChatColor.valueOf(plugin.config.colors.statValue))
                .append(Util.transformTime(res ?: 0))
                .append(ChatColor.RESET)
        }

        return sb.toString()
    }

    protected fun topListDistanceResponse(name: String, list: List<Tuple>): String {
        val sb = StringBuilder()

        sb  .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("- ")
            .append(name)
            .append(' ')
            .append(ChatColor.valueOf(plugin.config.colors.statSeparator))
            .append("| ")
            .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append("Top ")
            .append(list.size)
            .append(" -")

        var i = 0

        for (tuple in list) {
            val res = tuple.get(1, Int::class.java)

            sb  .append('\n')
                .append(ChatColor.RESET)
                .append(ChatColor.BOLD)
                .append(ChatColor.valueOf(plugin.config.colors.listNumber))
                .append(++i)
                .append(". ")
                .append(ChatColor.RESET)
                .append(ChatColor.valueOf(plugin.config.colors.playerName))
                .append(tuple.get(0, String::class.java))
                .append(ChatColor.WHITE)
                .append(": ")
                .append(ChatColor.valueOf(plugin.config.colors.statValue))
                .append(Util.distanceUnits(res ?: 0))
                .append(ChatColor.RESET)
        }

        return sb.toString()
    }

    protected fun getId(name: String) =
        if (plugin.players.containsKey(name)) {
            plugin.databaseManager.getPlayerId(plugin.players[name]!!)
        } else {
            plugin.databaseManager.getPlayerId(name)
        }
}
