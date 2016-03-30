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
import com.demonwav.statcraft.querydsl.QItemPickups;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCItemsPickedUp extends SCTemplate {

    public SCItemsPickedUp(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("itemspickedup", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.itemspickedup");
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
            QItemPickups i = QItemPickups.itemPickups;
            Integer result = query.from(i).where(i.id.eq(id)).uniqueResult(i.amount.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Items Picked Up")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Items Picked Up")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QItemPickups i = QItemPickups.itemPickups;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(i)
            .leftJoin(p)
            .on(i.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(i.amount.sum().desc())
            .limit(num)
            .list(p.name, i.amount.sum());

        return topListResponse("Items Picked Up", list);
    }
}
