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
import com.demonwav.statcraft.magic.FishCode;
import com.demonwav.statcraft.querydsl.QFishCaught;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCFishCaught extends SCTemplate {

    public SCFishCaught(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("fishcaught", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.fishcaught");
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
            QFishCaught f = QFishCaught.fishCaught;
            List<Tuple> list = query.from(f).where(f.id.eq(id)).groupBy(f.type).list(f.type, f.amount.sum());

            int total;
            int fish = 0;
            int treasure = 0;
            int junk = 0;

            for (Tuple tuple : list) {
                Byte type = tuple.get(f.type);

                if (type == null)
                    continue;

                FishCode code = FishCode.fromCode(type);

                if (code == null)
                    continue;

                Integer sum = tuple.get(f.amount.sum());
                sum = sum == null ? 0 : sum;
                switch (code) {
                    case FISH:
                        fish = sum;
                        break;
                    case TREASURE:
                        treasure = sum;
                        break;
                    case JUNK:
                        junk = sum;
                        break;
                }
            }

            total = fish + treasure + junk;

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Fish Caught")
                .addStat("Total", df.format(total))
                .addStat("Fish", df.format(fish))
                .addStat("Treasure", df.format(treasure))
                .addStat("Junk", df.format(junk))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Fish Caught")
                .addStat("Total", String.valueOf(0))
                .addStat("Fish", String.valueOf(0))
                .addStat("Treasure", String.valueOf(0))
                .addStat("Junk", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QFishCaught f = QFishCaught.fishCaught;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(f)
            .leftJoin(p)
            .on(f.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(f.amount.sum().desc())
            .limit(num)
            .list(p.name, f.amount.sum());

        return topListResponse("Fish Caught", list);
    }
}
