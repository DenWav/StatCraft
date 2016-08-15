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
import com.demonwav.statcraft.querydsl.QOnFire;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCOnFire extends SCTemplate {

    public SCOnFire(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("onfire", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.onfire");
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
            QOnFire o = QOnFire.onFire;
            Integer result = query.from(o).where(o.id.eq(id)).uniqueResult(o.time.sum());

            return new TimeResponseBuilder(plugin)
                .setName(name)
                .setStatName("On Fire")
                .addStat("Total", String.valueOf(result))
                .toString();

        } catch (Exception e) {
            return new TimeResponseBuilder(plugin)
                .setName(name)
                .setStatName("On Fire")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QOnFire o = QOnFire.onFire;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(o)
            .leftJoin(p)
            .on(o.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(o.time.sum().desc())
            .limit(num)
            .list(p.name, o.time.sum());

        return topListTimeResponse("On Fire", list);
    }
}
