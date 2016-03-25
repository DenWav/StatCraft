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
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QTabComplete;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCTabCompletes extends SCTemplate {

    public SCTabCompletes(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("tabcompletes", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.tabcompletes");
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
            QTabComplete t = QTabComplete.tabComplete;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount);

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Tab Completes")
                    .addStat("Total", df.format(result))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Tab Completes")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QTabComplete t = QTabComplete.tabComplete;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
                .from(t)
                .leftJoin(p)
                .on(t.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(t.amount.desc())
                .limit(num)
                .list(p.name, t.amount);

        return topListResponse("Tab Completes", list);
    }
}
