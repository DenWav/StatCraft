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
import com.demonwav.statcraft.commands.TimeResponseBuilder;
import com.demonwav.statcraft.querydsl.QPlayTime;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QSeen;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class SCPlayTime extends SCTemplate {

    public SCPlayTime(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("playtime", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.playtime");
    }

    @Override
    public String playerStatResponse(String name, List<String> args) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QPlayTime t = QPlayTime.playTime;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount);

            if (result == null)
                result = 0;

            UUID uuid = plugin.players.get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player != null && player.isOnline()) {
                int now = (int)(System.currentTimeMillis() / 1000L);

                QSeen s = QSeen.seen;
                query = plugin.getDatabaseManager().getNewQuery();
                Integer join = query.from(s).where(s.id.eq(id)).uniqueResult(s.lastJoinTime);

                // Sanity check
                if (join != null && join != 0 && now != 0)
                    result = result + (now - join);
            }

            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Play Time")
                    .addStat("Total", String.valueOf(result))
                    .toString();
        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Play Time")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QPlayTime t = QPlayTime.playTime;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(t)
                .leftJoin(p)
                .on(t.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(t.amount.desc())
                .limit(num)
                .list(p.name, t.amount);

        return topListTimeResponse("Play Time", list);
    }
}
