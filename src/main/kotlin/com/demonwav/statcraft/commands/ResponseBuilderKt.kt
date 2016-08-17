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
import org.bukkit.ChatColor
import java.util.LinkedHashMap

// We'll remove the Kt once we've totally moved away from Java
open class ResponseBuilderKt(protected val plugin: StatCraft) {

    protected var name = ""
    protected var statName = ""
    val stats = LinkedHashMap<String, String>()

    inline fun playerName(name: () -> String) {
        this.name = name()
    }

    inline fun statName(statName: () -> String) {
        this.statName = statName()
    }

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
                .append(entry.value)

            if (hasNext()) {
                sb.append(ChatColor.valueOf(plugin.config.colors.statSeparator)).append(" | ")
            }
        }

        return sb.toString()
    }

    companion object {
        inline fun build(plugin: StatCraft, builder: ResponseBuilderKt.() -> Unit): String {
            val inst = ResponseBuilderKt(plugin)

            inst.builder()
            return inst.toString()
        }
    }
}
