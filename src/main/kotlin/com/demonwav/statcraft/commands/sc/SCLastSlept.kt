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
import com.demonwav.statcraft.querydsl.QSleep
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class SCLastSlept(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        plugin.baseCommand.registerCommand("lastslept", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.lastslept")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        try {
            var uuid = plugin.players[name]
            val player = if (uuid == null) {
                plugin.server.getOfflinePlayer(name)
            } else {
                plugin.server.getOfflinePlayer(uuid)
            }

            if (player.isOnline && player.player.isSleeping) {
                return "${ChatColor.valueOf(plugin.config.colors.playerName)}$name${ChatColor.valueOf(plugin.config.colors.statValue)}" +
                    " is sleeping now!"
            } else {
                if (uuid == null && player.uniqueId.version() < 4) {
                    throw Exception()
                } else {
                    uuid = player.uniqueId
                }

                val id = plugin.databaseManager.getPlayerId(uuid) ?: throw Exception()

                val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError
                val s = QSleep.sleep

                val result = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed.max()) ?: throw Exception()

                val date = Date(result.toLong() * 1000L)
                val format = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz")
                format.timeZone = TimeZone.getTimeZone(plugin.timeZone)

                val now = Date()
                val difference = now.time - date.time

                val time = "${format.format(date)} (${Util.transformTime((difference / 1000L).toInt()).split(",".toRegex())[0]} ago)"

                return "${ChatColor.valueOf(plugin.config.colors.playerName)}$name${ChatColor.valueOf(plugin.config.colors.statTitle)}" +
                    " - Last Slept - ${ChatColor.valueOf(plugin.config.colors.statValue)}$time"
            }
        } catch (e: Exception) {
            return "${ChatColor.valueOf(plugin.config.colors.playerName)}$name${ChatColor.valueOf(plugin.config.colors.statValue)}" +
                " has not slept on this server."
        }
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        return null;
    }
}
