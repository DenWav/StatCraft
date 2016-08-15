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
import com.demonwav.statcraft.querydsl.QKills;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCKills extends SCTemplate {

    public SCKills(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("kills", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.kills");
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
            QKills k = QKills.kills;
            Integer result = query.from(k).where(k.id.eq(id)).uniqueResult(k.amount.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Kills")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Kills")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QKills k = QKills.kills;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(k)
            .leftJoin(p)
            .on(k.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(k.amount.sum().desc())
            .limit(num)
            .list(p.name, k.amount.sum());

        return topListResponse("Kills", list);
    }
}
