/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;

import org.bukkit.ChatColor;

import java.util.Iterator;
import java.util.Map;

public class TimeResponseBuilder extends ResponseBuilder {

    public TimeResponseBuilder(final StatCraft plugin) {
        super(plugin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb  .append(ChatColor.valueOf(plugin.config().getColors().getPlayerName()))
            .append(name)
            .append(ChatColor.valueOf(plugin.config().getColors().getStatTitle()))
            .append(" - ").append(statName).append(" - ");

        Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb  .append(ChatColor.valueOf(plugin.config().getColors().getStatLabel()))
                .append(entry.getKey()).append(": ")
                .append(ChatColor.valueOf(plugin.config().getColors().getStatValue()))
                .append(Util.transformTime(Integer.parseInt(entry.getValue())));

            if (iterator.hasNext())
                sb.append(ChatColor.valueOf(plugin.config().getColors().getStatSeparator())).append(" | ");
        }

        return sb.toString();
    }
}
