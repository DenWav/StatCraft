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
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.querydsl.QBlockBreak;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCBlocksBroken extends SCTemplate {

    public SCBlocksBroken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("blocksbroken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.blocksbroken");
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
            QBlockBreak b = QBlockBreak.blockBreak;
            Integer total = query.from(b).where(b.id.eq(id)).uniqueResult(b.amount.sum());

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Blocks Broken")
                    .addStat("Total", df.format(total == null ? 0 : total))
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Blocks Broken")
                    .addStat("Total", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QBlockBreak b = QBlockBreak.blockBreak;
        QPlayers p = QPlayers.players;
        List<Tuple> result = query
                .from(b)
                .leftJoin(p)
                .on(b.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(b.amount.sum().desc())
                .limit(num)
                .list(p.name, b.amount.sum());

        return topListResponse("Blocks Broken", result);
    }
}
