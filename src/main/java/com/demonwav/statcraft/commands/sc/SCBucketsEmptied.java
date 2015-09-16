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
import com.demonwav.statcraft.magic.BucketCode;
import com.demonwav.statcraft.querydsl.BucketEmpty;
import com.demonwav.statcraft.querydsl.QBucketEmpty;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SCBucketsEmptied extends SCTemplate {

    public SCBucketsEmptied(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("bucketsemptied", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.bucketsemptied");
    }

    @Override
    public String playerStatResponse(String name, List<String> args) {
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";
            QBucketEmpty e = QBucketEmpty.bucketEmpty;
            List<BucketEmpty> results = query.from(e).where(e.id.eq(id)).list(e);

            int total;
            int water = 0;
            int lava = 0;
            int milk = 0;

            for (BucketEmpty bucketEmpty : results) {
                BucketCode code = BucketCode.fromCode(bucketEmpty.getType());

                if (code == null)
                    continue;

                switch (code) {
                    case WATER:
                        water = bucketEmpty.getAmount();
                        break;
                    case LAVA:
                        lava = bucketEmpty.getAmount();
                        break;
                    case MILK:
                        milk = bucketEmpty.getAmount();
                        break;
                }
            }

            total = water + lava + milk;

            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Buckets Emptied")
                    .addStat("Total", df.format(total))
                    .addStat("Water", df.format(water))
                    .addStat("Lava", df.format(lava))
                    .addStat("Milk", df.format(milk))
                    .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Buckets Emptied")
                    .addStat("Total", String.valueOf(0))
                    .addStat("Water", String.valueOf(0))
                    .addStat("Lava", String.valueOf(0))
                    .addStat("Milk", String.valueOf(0))
                    .toString();
        }
    }

    @Override
    public String serverStatListResponse(int num, List<String> args) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QBucketEmpty e = QBucketEmpty.bucketEmpty;
        QPlayers p = QPlayers.players;
        List<Tuple> result = query
                .from(e)
                .leftJoin(p)
                .on(e.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(e.amount.sum().desc())
                .limit(num)
                .list(p.name, e.amount.sum());

        return topListResponse("Buckets Emptied", result);
    }
}
