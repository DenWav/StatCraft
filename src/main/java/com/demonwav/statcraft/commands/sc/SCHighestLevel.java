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
import com.demonwav.statcraft.querydsl.QHighestLevel;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCHighestLevel extends SCTemplate {

    public SCHighestLevel(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("highestlevel", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.highestlevel");
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
            QHighestLevel h = QHighestLevel.highestLevel;
            Integer result = query.from(h).where(h.id.eq(id)).uniqueResult(h.level);

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Highest Level")
                .addStat("Level", df.format(result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Highest Level")
                .addStat("Level", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(long num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QHighestLevel h = QHighestLevel.highestLevel;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(h)
            .leftJoin(p)
            .on(h.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(h.level.desc())
            .limit(num)
            .list(p.name, h.level);

        return topListResponse("Highest Level", list);
    }
}
