/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.commands.sc.SCTemplate
import com.demonwav.statcraft.iter
import com.demonwav.statcraft.use
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.event.Listener
import java.sql.Connection
import java.sql.SQLException
import java.util.Collections
import java.util.LinkedList
import java.util.TreeMap

class BaseCommand(private val plugin: StatCraft) : CommandExecutor, TabCompleter, Listener {

    private val subCommands = TreeMap<String, SCTemplate>()

    fun registerCommand(cmd: String, command: SCTemplate) {
        if (subCommands.containsKey(cmd)) {
            throw CommandAlreadyDefinedException(cmd)
        }
        subCommands.put(cmd, command)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): Boolean {
        if (args.size == 0) {
            sender.sendMessage("${ChatColor.GRAY}Available Commands: ")
            val stringBuilder = StringBuilder()
            subCommands.entries.iter {
                if (it.value.hasPermission(sender, args)) {
                    // Only show them commands they are allowed to run
                    stringBuilder.append(it.key)
                    if (hasNext()) {
                        stringBuilder.append(ChatColor.AQUA.toString())
                        stringBuilder.append(", ")
                        stringBuilder.append(ChatColor.RESET.toString())
                    }
                }
            }
            sender.sendMessage(stringBuilder.toString())
        } else {
            if (subCommands.containsKey(args[0])) {
                val subArgs = Array(args.size - 1, {""})
                System.arraycopy(args, 1, subArgs, 0, subArgs.size)
                if (subCommands[args[0]]?.hasPermission(sender, subArgs) == true) {
                    respondToCommand(sender, subArgs, subCommands[args[0]]!!)
                } else {
                    sender.sendMessage("You don't have permission to run this command.")
                }
            } else {
                sender.sendMessage("Command not found.")
            }
        }
        return true
    }

    private fun respondToCommand(sender: CommandSender, args: Array<String>, command: SCTemplate) {
        if (command is CustomResponse) {
            command.respondToCommand(sender, args)
        } else {
            // control variables
            var publicCmd = false
            var top = false
            var secondaryArgs: Array<String>? = null
            val secondaryArgsList = LinkedList<String>()
            val players = LinkedList<String>()

            // if type == true, this is how many to display
            var topNumber = 0

            // look for -all and -top# arguments
            args.forEach { arg ->
                // if we find a -all argument then set top to true, regardless of how many we find or where it's located
                if (arg == "-all") {
                    publicCmd = true
                } else if (arg.startsWith("-top")) {
                    top = true

                    // check if it is valid, first remove -top from the front and then check for integers
                    try {
                        topNumber = arg.substring(4).toInt()
                        // this was successful, so nothing more needs to be done
                    } catch (e: NumberFormatException) {
                        // the argument was invalid, so show an error and exit
                        sender.sendMessage("Not a valid \"-top\" value. Please use \"-top#\" with # being an integer.")
                        return
                    }
                } else if (!arg.startsWith("-")) {
                    players.add(arg)
                } else {
                    secondaryArgsList.add(arg.substring(1))
                }
            }

            val clazz = command.javaClass
            val method = if (top) {
                clazz.getMethod("serverStatListResponse", Int::class.java, List::class.java, Connection::class.java)
            } else {
                clazz.getMethod("playerStatResponse", String::class.java, List::class.java, Connection::class.java)
            }

            val annotation = method.getAnnotation(SecondaryArgument::class.java)
            if (annotation != null) {
                secondaryArgs = annotation.value
            }

            if (secondaryArgs != null) {
                secondaryArgsList.retainAll(secondaryArgs)
            } else {
                secondaryArgsList.retainAll(Collections.emptyList())
            }

            if (players.size == 0) {
                players.add(sender.name)
            }

            // Asynchronously access the database and calculate hte result, then call a sync task to return the output
            plugin.server.scheduler.runTaskAsynchronously(plugin) {
                if (top) {
                    try {
                        plugin.databaseManager.connection.use {
                            val response = command.serverStatListResponse(topNumber, secondaryArgsList, this)

                            plugin.server.scheduler.runTask(plugin) {
                                if (response == null) {
                                    sender.sendMessage("\"-top\" cannot be used with this command.")
                                } else {
                                    if (publicCmd) {
                                        val endResponse =
                                            "${ChatColor.valueOf(plugin.config.colors.publicIdentifier)}@${sender.name}${ChatColor.WHITE}: $response"
                                        plugin.server.broadcastMessage(endResponse)
                                    } else {
                                        sender.sendMessage(response)
                                    }
                                }
                            }
                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                } else {
                    plugin.server.scheduler.runTask(plugin) {
                        try {
                            plugin.databaseManager.connection.use {
                                players.forEach { player ->
                                    val response = command.playerStatResponse(player, secondaryArgsList, this)

                                    if (publicCmd) {
                                        val endResponse =
                                            "${ChatColor.valueOf(plugin.config.colors.publicIdentifier)}@${sender.name}${ChatColor.WHITE}: $response"
                                        plugin.server.broadcastMessage(endResponse)
                                    } else {
                                        sender.sendMessage(response)
                                    }
                                }
                            }
                        } catch (e: SQLException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            // Return a list of only the commands they are allowed to run
            val result = subCommands.entries
                .filter { it.value.hasPermission(sender, null) && it.key.startsWith(args[0]) }
                .map { it.key }

            return result.sortedWith(String.CASE_INSENSITIVE_ORDER)
        } else {
            if (subCommands.containsKey(args[0])) {
                val subArgs = Array(args.size - 1, {""})
                System.arraycopy(args, 1, subArgs, 0, subArgs.size)
                if (subCommands[args[0]]?.hasPermission(sender, subArgs) == true) {
                    return subCommands[args[0]]?.onTabComplete(sender, subArgs) ?: Collections.emptyList()
                } else {
                    return Collections.emptyList()
                }
            } else {
                return Collections.emptyList()
            }
        }
    }
}
