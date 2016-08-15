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
import com.demonwav.statcraft.querydsl.QSleep;

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

public class SCLastSlept extends SCTemplate {

    public SCLastSlept(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("lastslept", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.lastslept");
    }

    @SuppressWarnings("deprecation")
    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        try {
            UUID uuid = plugin.getPlayers().get(name);
            OfflinePlayer player;
            if (uuid == null) {
                player = plugin.getServer().getOfflinePlayer(name);
            } else {
                player = plugin.getServer().getOfflinePlayer(uuid);
            }

            if (player.isOnline() && player.getPlayer().isSleeping()) {
                return ChatColor.valueOf(plugin.getConfig().getColors().getPlayerName()) +
                    name + ChatColor.valueOf(plugin.getConfig().getColors().getStatValue()) +
                    " is sleeping now!";
            } else {
                // only use this new UUID if it's a real player
                if (uuid == null && player.getUniqueId().version() < 4) {
                    throw new Exception();
                } else  if (uuid == null) {
                    uuid = player.getUniqueId();
                }

                int id = plugin.getDatabaseManager().getPlayerId(uuid);
                if (id < 0) {
                    throw new Exception();
                }

                SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
                if (query == null)
                    return "Sorry, there seems to be an issue connecting to the database right now.";
                QSleep s = QSleep.sleep;

                Integer result = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed.max());
                if (result == null) {
                    throw new Exception();
                }

                plugin.getServer().getLogger().info(String.valueOf(result));

                Date date = new Date(((long) result) * 1000L);
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm aa zzz");
                format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                String time = format.format(date);

                Date now = new Date();
                long difference = now.getTime() - date.getTime();

                time = time + " (" + Util.transformTime((int) (difference / 1000L)).split(",")[0] + " ago)";

                return ChatColor.valueOf(plugin.getConfig().getColors().getPlayerName()) + name +
                    ChatColor.valueOf(plugin.getConfig().getColors().getStatTitle()) + " - Last Slept - " +
                    ChatColor.valueOf(plugin.getConfig().getColors().getStatValue()) + time;
            }
        } catch (Exception e) {
            return ChatColor.valueOf(plugin.getConfig().getColors().getPlayerName()) +
                name + ChatColor.valueOf(plugin.getConfig().getColors().getStatValue()) +
                " has not slept on this server.";
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        return null;
    }
}
