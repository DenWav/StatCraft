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
import com.demonwav.statcraft.querydsl.QItemDrops;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCItemsDropped extends SCTemplate {

    public SCItemsDropped(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("itemsdropped", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.itemsdropped");
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
            QItemDrops i = QItemDrops.itemDrops;
            Integer result = query.from(i).where(i.id.eq(id)).uniqueResult(i.amount.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Items Dropped")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Items Dropped")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(long num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QItemDrops i = QItemDrops.itemDrops;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(i)
            .leftJoin(p)
            .on(i.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(i.amount.sum().desc())
            .limit(num)
            .list(p.name, i.amount.sum());

        return topListResponse("Items Dropped", list);
    }
}
