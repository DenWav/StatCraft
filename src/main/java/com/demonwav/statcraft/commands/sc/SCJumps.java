/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands.sc;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.querydsl.QJumps;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCJumps extends SCTemplate {

    public SCJumps(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("jumps", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.jumps");
    }

    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        try {
            int id = getId(name);
            if (id < 0)
                throw new Exception();

            QJumps j = QJumps.jumps;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            Integer result = query.from(j).where(j.id.eq(id)).uniqueResult(j.amount.sum());
            if (result == null)
                throw new Exception();

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Jumps")
                .addStat("Total", df.format(result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Jumps")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        QJumps j = QJumps.jumps;
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";

        List<Tuple> list = query
            .from(j)
            .leftJoin(p)
            .on(j.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(j.amount.sum().desc())
            .limit(num)
            .list(p.name, j.amount.sum());

        return topListResponse("Jumps", list);
    }
}
