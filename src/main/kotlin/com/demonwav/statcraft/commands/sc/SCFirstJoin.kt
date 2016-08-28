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
import com.demonwav.statcraft.querydsl.QSeen
import com.demonwav.statcraft.transformTime
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class SCFirstJoin(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("firstjoin", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.firstjoin")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        val id = getId(name) ?: return "${ChatColor.valueOf(plugin.config.colors.playerName)}$name" +
            "${ChatColor.valueOf(plugin.config.colors.statValue)} has not been seen on this server."

        val s = QSeen.seen
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError

        val result = query.from(s).where(s.id.eq(id)).uniqueResult(s.firstJoinTime) ?: throw Exception()

        val date = Date(result.toLong() * 1000L)
        val format = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz")
        format.timeZone = TimeZone.getTimeZone(plugin.timeZone)
        var time = format.format(date)

        val now = Date()
        val difference = now.time - date.time

        time = "$time (${transformTime((difference / 1000L).toInt()).split(",".toRegex())[0]} ago)"

        return "${ChatColor.valueOf(plugin.config.colors.playerName)}$name" +
            "${ChatColor.valueOf(plugin.config.colors.statTitle)} - First Join - " +
            "${ChatColor.valueOf(plugin.config.colors.statValue)}$time"
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        return null
    }
}
