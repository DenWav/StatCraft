/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands.sc;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.querydsl.QSeen;

import com.mysema.query.sql.SQLQuery;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class SCLastSeen extends SCTemplate {

    public SCLastSeen(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("lastseen", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.lastseen");
    }

    @SuppressWarnings("deprecation")
    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        try {
            UUID uuid = plugin.players.get(name);
            OfflinePlayer player;
            if (uuid == null) {
                player = plugin.getServer().getOfflinePlayer(name);
            } else {
                player = plugin.getServer().getOfflinePlayer(uuid);
            }

            if (player.isOnline()) {
                return ChatColor.valueOf(plugin.config().getColors().getPlayerName()) +
                    name + ChatColor.valueOf(plugin.config().getColors().getStatValue()) +
                    " is online now!";
            } else {
                // only use this new UUID if it's a real player
                if (uuid == null && player.getUniqueId().version() < 4) {
                    throw new Exception();
                } else if (uuid == null) {
                    uuid = player.getUniqueId();
                }

                int id = plugin.getDatabaseManager().getPlayerId(uuid);
                if (id < 0) {
                    throw new Exception();
                }

                SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
                if (query == null)
                    return "Sorry, there seems to be an issue connecting to the database right now.";
                QSeen s = QSeen.seen;

                Integer result = query.from(s).where(s.id.eq(id)).uniqueResult(s.lastLeaveTime);
                if (result == null) {
                    throw new Exception();
                }

                Date date = new Date(((long) result) * 1000L);
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
                format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                String time = format.format(date);

                Date now = new Date();
                long difference = now.getTime() - date.getTime();

                time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

                return ChatColor.valueOf(plugin.config().getColors().getPlayerName()) + name +
                    ChatColor.valueOf(plugin.config().getColors().getStatTitle()) + " - Last Seen - " +
                    ChatColor.valueOf(plugin.config().getColors().getStatValue()) + time;
            }
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
