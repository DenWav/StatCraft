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
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.querydsl.QJoins;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCJoins extends SCTemplate {

    public SCJoins(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("joins", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.joins");
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
            QJoins j = QJoins.joins;

            Integer result = query.from(j).where(j.id.eq(id)).uniqueResult(j.amount.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Joins")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Joins")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QJoins j = QJoins.joins;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(j)
            .leftJoin(p)
            .groupBy(p.name)
            .orderBy(j.amount.desc())
            .limit(num)
            .list(p.name, j.amount);

        return topListResponse("Joins", list);
    }
}
