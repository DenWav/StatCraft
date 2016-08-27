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
import com.demonwav.statcraft.Table
import com.demonwav.statcraft.commands.CustomResponse
import com.demonwav.statcraft.querydsl.QSeen
import com.demonwav.statcraft.querydsl.QSleep
import com.demonwav.statcraft.use
import org.apache.commons.lang.StringEscapeUtils
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.UUID

class SCReset(plugin: StatCraft) : SCTemplate(plugin), CustomResponse {

    private val map = HashMap<CommandSender, UUID>()

    init {
        plugin.baseCommand.registerCommand("resetstats", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?): Boolean {
        if (args == null || args.size == 0 || args[0].equals("yes", true) || args[0].equals("cancel", true)) {
            return sender.hasPermission("statcraft.user.resetstats")
        } else {
            return sender.hasPermission("statcraft.admin.resetotherstats")
        }
    }

    override fun respondToCommand(sender: CommandSender, args: Array<String>) {
        if (args.size == 1 && (args[0].equals("yes", true) || args[0].equals("cancel", true)) && map.containsKey(sender)) {
            if (args[0].equals("cancel", true)) {
                if (map.containsKey(sender)) {
                    sender.sendMessage("Reset stats request canceled.")
                }
            } else {
                val uuid = map[sender]

                if (uuid == plugin.players[sender.name]) {
                    // this person is resetting their own stats
                    // check permissions one more time
                    if (sender.hasPermission("statcraft.user.resetstats")) {
                        resetStats(sender, uuid!!, sender.name)
                    } else {
                        sender.sendMessage("Yout don't have permission to reset your own stats.")
                    }
                } else {
                    // This person is resetting someone else's stats
                    // Check permissions one more time
                    if (sender.hasPermission("statcraft.admin.resetotherstats")) {
                        var name: String? = null
                        for ((key, value) in plugin.players) {
                            if (value == uuid) {
                                name = key
                                break
                            }
                        }

                        if (name != null) {
                            resetStats(sender, uuid!!, name)
                        } else {
                            resetStats(sender, uuid!!, "")
                        }
                    } else {
                        sender.sendMessage("You don't have permission to reset someone else's stats.")
                    }
                }
            }
            map.remove(sender)
        } else {
            if (args.size == 0 && sender is OfflinePlayer) {
                sender.sendMessage("Are you sure you want to reset your own stats?\n" +
                    "Run command: ${ChatColor.GRAY}${ChatColor.ITALIC}/sc resetstats yes${ChatColor.RESET} to verify.")
                sender.sendMessage("Run command: ${ChatColor.GRAY}${ChatColor.ITALIC}/sc resetstats cancel${ChatColor.RESET} to cancel.")
                map[sender] = sender.uniqueId
            } else if (args.size == 0) {
                sender.sendMessage("You must be a player to reset your own stats.")
            } else if (args.size == 1) {
                if (plugin.players.containsKey(args[0])) {
                    sender.sendMessage("Are you sure you want to reset ${args[0]}'s stats?\n" +
                        "Run command: ${ChatColor.GRAY}${ChatColor.ITALIC}/sc resetstats yes${ChatColor.RESET} to verify.")
                    sender.sendMessage("Run command: ${ChatColor.GRAY}${ChatColor.ITALIC}/sc resetstats cancel${ChatColor.RESET} to cancel.")
                    map[sender] = plugin.players[args[0]]!!
                } else {
                    sender.sendMessage("${args[0]} was not found.")
                }
            } else {
                sender.sendMessage("Usage: /sc resetstats [player]")
            }
        }
    }

    private fun resetStats(sender: CommandSender, uuid: UUID, name: String) {
        plugin.server.scheduler.runTaskAsynchronously(plugin) {
            val id = plugin.databaseManager.getPlayerId(uuid) ?:
                return@runTaskAsynchronously sender.sendMessage("Unable to find $name in the datbase.")

            var st: Statement? = null
            try {
                plugin.databaseManager.connection.use {
                    try {
                        autoCommit = false
                        st = createStatement()

                        for (table in Table.values()) {
                            if (!table.getName().equals("players", true)) {
                                val tableName = StringEscapeUtils.escapeSql(table.getName())
                                st?.addBatch("DELETE FROM $tableName WHERE $tableName id = $id;")
                            }
                        }

                        st?.executeBatch()

                        commit()
                        autoCommit = true
                    } catch (e: SQLException) {
                        try {
                            rollback()
                            autoCommit = true
                        } catch (e1: SQLException) {
                            e1.printStackTrace()
                        }
                    } finally {
                        try {
                            st?.close()
                        } catch (e: SQLException) {
                            e.printStackTrace()
                        }
                    }

                    // So we don't mess up play time / time slept, check if they are online or in the bed
                    // and add "join" values for now
                    val player = plugin.server.getPlayer(uuid)
                    if (player != null && player.isOnline) {
                        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

                        val s = QSeen.seen
                        val query = plugin.databaseManager.getNewQuery(this)

                        if (query != null && query.from(s).where(s.id.eq(id)).exists()) {
                            val clause = plugin.databaseManager.getUpdateClause(this, s)
                            clause?.where(s.id.eq(id))?.set(s.lastJoinTime, currentTime)?.execute()
                        } else {
                            val clause = plugin.databaseManager.getInsertClause(this, s)
                            clause?.columns(s.id, s.lastJoinTime)?.values(id, currentTime)?.execute()
                        }

                        if (player.player.isSleeping) {
                            val sl = QSleep.sleep

                            if (query != null && query.from(sl).where(sl.id.eq(id)).exists()) {
                                val clause = plugin.databaseManager.getUpdateClause(this, sl)
                                clause?.where(sl.id.eq(id))?.set(sl.enterBed, currentTime)?.execute()
                            } else {
                                val clause = plugin.databaseManager.getInsertClause(this, sl)
                                clause?.columns(sl.id, sl.enterBed)?.values(id, currentTime)?.execute()
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            if (sender.name.equals(name)) {
                sender.sendMessage("Your stats have been successfully reset.")
            } else {
                sender.sendMessage("$name's stats have been successfully reset.")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            if (map.containsKey(sender)) {
                val list = ArrayList<String>()

                if ("cancel".startsWith(args[0].toLowerCase())) {
                    list.add("cancel")
                }
                if ("yes".startsWith(args[0].toLowerCase())) {
                    list.add("yes")
                }
                return list
            } else {
                val players = ArrayList<String>(plugin.players.keys)
                var secondary = plugin.server.onlinePlayers.map { it.name }

                // keep only the offline players for now
                players.removeAll(secondary)

                // primarily we want players that are online
                val result = secondary.filter { it.toLowerCase().startsWith(args[args.size - 1].toLowerCase()) }.toMutableList()

                // also include players that are offline
                secondary = players.filter { it.toLowerCase().startsWith(args[args.size - 1].toLowerCase()) }

                // They need to be sorted independently
                result.sortedWith(String.CASE_INSENSITIVE_ORDER)
                secondary.sortedWith(String.CASE_INSENSITIVE_ORDER)
                // Add the offline players to the end
                result.addAll(secondary)
                return result
            }
        }

        return Collections.emptyList()
    }

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String? {
        return null
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        return null
    }
}
