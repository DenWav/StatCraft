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
import com.demonwav.statcraft.commands.TimeResponseBuilder;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QSleep;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class SCTimeSlept extends SCTemplate {

    public SCTimeSlept(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("timeslept", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.bed");
    }

    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        try {
            int id = getId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QSleep s = QSleep.sleep;
            Integer result = query.from(s).where(s.id.eq(id)).uniqueResult(s.timeSlept);

            if (result == null)
                result = 0;

            UUID uuid = plugin.getPlayers().get(name);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            if (player.isOnline() && player.getPlayer().isSleeping()) {
                int now = (int)(System.currentTimeMillis() / 1000L);

                query = plugin.getDatabaseManager().getNewQuery(connection);
                if (query != null) {
                    Integer enter = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed);

                    // Sanity check
                    if (enter != null && enter != 0 && now != 0) {
                        result = result + (now - enter);
                    }
                }
            }

            return new TimeResponseBuilder(plugin)
                .setName(name)
                .setStatName("Time Slept")
                .addStat("Total", String.valueOf(result))
                .toString();
        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                .setName(name)
                .setStatName("Time Slept")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(long num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QSleep s = QSleep.sleep;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(s)
            .leftJoin(p)
            .on(s.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(s.timeSlept.sum().desc())
            .limit(num)
            .list(p.name, s.timeSlept.sum());

        return topListTimeResponse("Time Slept", list);
    }
}
