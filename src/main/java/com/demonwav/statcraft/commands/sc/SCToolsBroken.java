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
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QToolsBroken;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCToolsBroken extends SCTemplate {

    public SCToolsBroken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("toolsbroken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.toolsbroken");
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
            QToolsBroken t = QToolsBroken.toolsBroken;
            Integer result = query.from(t).where(t.id.eq(id)).uniqueResult(t.amount.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Tools Broken")
                .addStat("Total", df.format(result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Tools Broken")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(long num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QToolsBroken t = QToolsBroken.toolsBroken;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(t)
            .leftJoin(p)
            .on(t.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(t.amount.sum().desc())
            .limit(num)
            .list(p.name, t.amount.sum());

        return topListResponse("Tools Broken", list);
    }
}
