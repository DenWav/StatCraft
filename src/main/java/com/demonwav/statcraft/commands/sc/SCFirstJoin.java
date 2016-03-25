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
import com.demonwav.statcraft.querydsl.QSeen;

import com.mysema.query.sql.SQLQuery;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SCFirstJoin extends SCTemplate {

    public SCFirstJoin(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("firstjoin", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.firstjoin");
    }

    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            QSeen s = QSeen.seen;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            Integer result = query.from(s).where(s.id.eq(id)).uniqueResult(s.firstJoinTime);
            if (result == null)
                throw new Exception();

            Date date = new Date(((long) result) * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
            format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
            String time = format.format(date);

            Date now = new Date();
            long difference = now.getTime() - date.getTime();

            time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

            return ChatColor.valueOf(plugin.config().getColors().getPlayerName()) + name +
                ChatColor.valueOf(plugin.config().getColors().getStatTitle()) + " - First Join - " +
                ChatColor.valueOf(plugin.config().getColors().getStatValue()) + time;
        } catch (Exception e) {
            return ChatColor.valueOf(plugin.config().getColors().getPlayerName()) +
                name + ChatColor.valueOf(plugin.config().getColors().getStatValue()) +
                " has not been seen on this server.";
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        return null;
    }
}
