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
import com.demonwav.statcraft.commands.ResponseBuilderKt
import com.demonwav.statcraft.querydsl.QDamageTaken
import com.demonwav.statcraft.querydsl.QPlayers
import org.bukkit.command.CommandSender
import java.sql.Connection

class SCDamageTaken(plugin: StatCraft) : SCTemplate(plugin) {

    init {
        this.plugin.baseCommand.registerCommand("damagetaken", this)
    }

    override fun hasPermission(sender: CommandSender, args: Array<out String>?) = sender.hasPermission("statcraft.user.damagetaken")

    override fun playerStatResponse(name: String, args: List<String>, connection: Connection): String {
        try {
            val id = getId(name) ?: throw Exception()

            val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError
            val d = QDamageTaken.damageTaken

            val total = query.from(d).where(d.id.eq(id)).uniqueResult(d.amount.sum()) ?: 0

            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Damage Taken" }
                stats["Total"] = df.format(total)
            }
        } catch (e: Exception) {
            return ResponseBuilderKt.build(plugin) {
                playerName { name }
                statName { "Damage Taken" }
                stats["Total"] = 0.toString()
            }
        }

    }

    override fun serverStatListResponse(num: Long, args: List<String>, connection: Connection): String? {
        val query = plugin.databaseManager.getNewQuery(connection) ?: return databaseError
        val d = QDamageTaken.damageTaken
        val p = QPlayers.players

        val list = query
            .from(d)
            .leftJoin(p)
            .on(d.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(d.amount.sum().desc())
            .limit(num)
            .list(p.name, d.amount.sum())

        return topListResponse("Damage Taken", list)
    }
}
