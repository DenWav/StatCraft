/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands;

import com.demonwav.statcraft.StatCraft;

import org.bukkit.ChatColor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseBuilder {

    protected StatCraft plugin;
    protected String name = "";
    protected String statName = "";
    protected LinkedHashMap<String, String> stats = new LinkedHashMap<>();

    public ResponseBuilder(StatCraft plugin) {
        this.plugin = plugin;
    }

    public ResponseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ResponseBuilder setStatName(String statName) {
        this.statName = statName;
        return this;
    }

    public ResponseBuilder addStat(String title, String value) {
        stats.put(title, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb      .append(ChatColor.valueOf(plugin.config().getColors().getPlayerName()))
                .append(name)
                .append(ChatColor.valueOf(plugin.config().getColors().getStatTitle()))
                .append(" - ").append(statName).append(" - ");

        Iterator<Map.Entry<String, String>> iterator = stats.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb      .append(ChatColor.valueOf(plugin.config().getColors().getStatLabel()))
                    .append(entry.getKey()).append(": ")
                    .append(ChatColor.valueOf(plugin.config().getColors().getStatValue()))
                    .append(entry.getValue());

            if (iterator.hasNext())
                sb.append(ChatColor.valueOf(plugin.config().getColors().getStatSeparator())).append(" | ");
        }

        return sb.toString();
    }
}

