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
import com.demonwav.statcraft.iter
import com.demonwav.statcraft.transformTime
import org.bukkit.ChatColor

class TimeResponseBuilder(plugin: StatCraft) : ResponseBuilder(plugin) {

    override fun toString(): String {
        val sb = StringBuilder()

        sb  .append(ChatColor.valueOf(plugin.config.colors.playerName))
            .append(name)
            .append(ChatColor.valueOf(plugin.config.colors.statTitle))
            .append(" - ")
            .append(statName)
            .append(" - ")

        stats.entries.iter { entry ->
            sb  .append(ChatColor.valueOf(plugin.config.colors.statLabel))
                .append(entry.key)
                .append(": ")
                .append(ChatColor.valueOf(plugin.config.colors.statValue))
                .append(transformTime(entry.value.toInt()))

            if (hasNext()) {
                sb.append(ChatColor.valueOf(plugin.config.colors.statSeparator)).append(" | ")
            }
        }

        return sb.toString()
    }

    companion object {
        inline fun build(plugin: StatCraft, builder: TimeResponseBuilder.() -> Unit): String {
            val inst = TimeResponseBuilder(plugin)

            inst.builder()
            return inst.toString()
        }
    }
}
