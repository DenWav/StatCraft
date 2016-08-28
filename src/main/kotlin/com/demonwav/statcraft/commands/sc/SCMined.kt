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
import com.demonwav.statcraft.commands.CustomResponse
import com.demonwav.statcraft.commands.ResponseBuilder
import com.demonwav.statcraft.querydsl.QBlockBreak
import com.demonwav.statcraft.use
import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.material.MaterialData
import java.sql.Connection
import java.sql.SQLException

class SCMined(plugin: StatCraft) : SCTemplate(plugin), CustomResponse {

    init {
        plugin.baseCommand.registerCommand("mined", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.mined")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String? {
        return null
    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        return null
    }

    override fun respondToCommand(sender: CommandSender, args: Array<String>) {
        if (args.size < 2 || args.size > 3) {
            sender.sendMessage("Usage: /sc mined <playername> <material|blockid|blockid:damage> [-all]")
            return
        }

        if (args.size == 3 && !args[2].equals("-all", true)) {
            sender.sendMessage("Usage: /sc mined <playername> <material|blockid|blockid:damage> [-all]")
            return
        }

        val name = args[0]
        val type = args[1]
        var blockid = 0
        var damage = 0
        val all = args.size == 3

        if (type.contains(":")) {
            val split = type.split(":")
            if (split.size != 2) {
                sender.sendMessage("Block id must follow format 'blockid' or 'blockid:damage'.")
                return
            }

            try {
                blockid = split[0].toInt()
            } catch (e: NumberFormatException) {
                val material = Material.getMaterial(split[0].toUpperCase().replace("\\s+".toRegex(), "_"))
                blockid = material.id
            }

            try {
                damage = split[1].toInt()
            } catch (e: NumberFormatException) {
                sender.sendMessage("Damage value must be an integer.")
                return
            }
        } else {
            try {
                blockid = type.toInt()
                damage = 0
            } catch (e: NumberFormatException) {
                val material = Material.getMaterial(type.toUpperCase().replace("\\s+".toRegex(), "_"))
                if (material != null) {
                    blockid = material.id
                    damage = MaterialData(material).data.toInt()
                }
            }
        }

        plugin.server.scheduler.runTaskAsynchronously(plugin) {
            try {
                plugin.databaseManager.connection.use {
                    var response = try {
                        val id = getId(name) ?: throw Exception()

                        val query = plugin.databaseManager.getNewQuery(this) ?: return@runTaskAsynchronously

                        val b = QBlockBreak.blockBreak

                        val result = if (damage == -1) {
                            query
                                .from(b)
                                .where(b.id.eq(id), b.blockid.eq(blockid.toShort()))
                                .uniqueResult(b.amount.sum())
                        } else {
                            query
                                .from(b)
                                .where(b.id.eq(id), b.blockid.eq(blockid.toShort()), b.damage.eq(damage.toShort()))
                                .uniqueResult(b.amount.sum())
                        } ?: 0

                        ResponseBuilder.build(plugin) {
                            playerName { name }
                            statName { WordUtils.capitalizeFully(type) + " mined" }
                            stats["Total"] = df.format(result)
                        }
                    } catch (e: Exception) {
                        ResponseBuilder.build(plugin) {
                            playerName { name }
                            statName { WordUtils.capitalizeFully(type) + " mined" }
                            stats["Total"] = "0"
                        }
                    }

                    if (all) {
                        response = "${ChatColor.valueOf(plugin.config.colors.publicIdentifier)}@${sender.name}: $response"
                    }

                    plugin.server.scheduler.runTask(plugin) {
                        if (all) {
                            plugin.server.broadcastMessage(response)
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
