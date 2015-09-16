/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands.sc;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;

import com.mysema.query.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class SCTemplate {

    protected StatCraft plugin;
    protected DecimalFormat df = new DecimalFormat("#,###");

    public SCTemplate(StatCraft plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        ArrayList<String> players = new ArrayList<>(plugin.players.keySet());
        ArrayList<String> secondary = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            secondary.add(player.getName());
        }

        players.removeAll(secondary);

        LinkedList<String> result = new LinkedList<>();

        for (String s : secondary) {
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                result.add(s);
        }
        for (String s : players) {
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                result.add(s);
        }
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public abstract boolean hasPermission(CommandSender sender, String[] args);

    public abstract String playerStatResponse(String name, List<String> args);

    public abstract String serverStatListResponse(int num, List<String> args);

    protected String topListResponse(String name, List<Tuple> list) {
        StringBuilder sb = new StringBuilder();

        sb  .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                .append("- ").append(name).append(" ")
                .append(ChatColor.valueOf(plugin.config().colors.stat_separator))
                .append("| ")
                .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                .append("Top ")
                .append(list.size())
                .append(" -");

        int i = 0;

        for (Tuple tuple : list) {
            sb      .append("\n")
                    .append(ChatColor.RESET)
                    .append(ChatColor.BOLD)
                    .append(ChatColor.valueOf(plugin.config().colors.list_number))
                    .append(++i)
                    .append(". ")
                    .append(ChatColor.RESET)
                    .append(ChatColor.valueOf(plugin.config().colors.player_name))
                    .append(tuple.get(0, String.class))
                    .append(ChatColor.WHITE)
                    .append(": ")
                    .append(ChatColor.valueOf(plugin.config().colors.stat_value))
                    .append(df.format(tuple.get(1, Integer.class)))
                    .append(ChatColor.RESET);
        }

        return sb.toString();
    }

    protected String topListTimeResponse(String name, List<Tuple> list) {
        StringBuilder sb = new StringBuilder();

        sb  .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                .append("- ").append(name).append(" ")
                .append(ChatColor.valueOf(plugin.config().colors.stat_separator))
                .append("| ")
                .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                .append("Top ")
                .append(list.size())
                .append(" -");

        int i = 0;

        for (Tuple tuple : list) {
            Integer res = tuple.get(1, Integer.class);
            sb      .append("\n")
                    .append(ChatColor.RESET)
                    .append(ChatColor.BOLD)
                    .append(ChatColor.valueOf(plugin.config().colors.list_number))
                    .append(++i)
                    .append(". ")
                    .append(ChatColor.RESET)
                    .append(ChatColor.valueOf(plugin.config().colors.player_name))
                    .append(tuple.get(0, String.class))
                    .append(ChatColor.WHITE)
                    .append(": ")
                    .append(ChatColor.valueOf(plugin.config().colors.stat_value))
                    .append(Util.transformTime(res == null ? 0 : res))
                    .append(ChatColor.RESET);
        }

        return sb.toString();
    }

    protected String topListDistanceResponse(String name, List<Tuple> list) {
        StringBuilder sb = new StringBuilder();

        sb  .append(ChatColor.valueOf(plugin.config().colors.stat_title))
            .append("- ").append(name).append(" ")
            .append(ChatColor.valueOf(plugin.config().colors.stat_separator))
            .append("| ")
            .append(ChatColor.valueOf(plugin.config().colors.stat_title))
            .append("Top ")
            .append(list.size())
            .append(" -");

        int i = 0;

        for (Tuple tuple : list) {
            Integer res = tuple.get(1, Integer.class);
            sb  .append("\n")
                .append(ChatColor.RESET)
                .append(ChatColor.BOLD)
                .append(ChatColor.valueOf(plugin.config().colors.list_number))
                .append(++i)
                .append(". ")
                .append(ChatColor.RESET)
                .append(ChatColor.valueOf(plugin.config().colors.player_name))
                .append(tuple.get(0, String.class))
                .append(ChatColor.WHITE)
                .append(": ")
                .append(ChatColor.valueOf(plugin.config().colors.stat_value))
                .append(Util.distanceUnits(res == null ? 0 : res))
                .append(ChatColor.RESET);
        }

        return sb.toString();
    }
}
