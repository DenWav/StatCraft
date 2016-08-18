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
import com.demonwav.statcraft.querydsl.QMessagesSpoken;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QWordFrequency;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.List;

public class SCWordsSpoken extends SCTemplate {

    public SCWordsSpoken(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("wordsspoken", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.wordsspoken");
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
            QMessagesSpoken m = QMessagesSpoken.messagesSpoken;
            Integer result = query.from(m).where(m.id.eq(id)).uniqueResult(m.wordsSpoken.sum());

            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Words Spoken")
                .addStat("Total", df.format(result == null ? 0 : result))
                .toString();
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Words Spoken")
                .addStat("Total", String.valueOf(0))
                .toString();
        }
    }

    @Override
    public String serverStatListResponse(long num, List<String> args, Connection connection) {
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";
        QWordFrequency w = QWordFrequency.wordFrequency;
        QPlayers p = QPlayers.players;

        List<Tuple> list = query
            .from(w)
            .leftJoin(p)
            .on(w.id.eq(p.id))
            .groupBy(p.name)
            .orderBy(w.amount.sum().desc())
            .limit(num)
            .list(p.name, w.amount.sum());

        return topListResponse("Words Spoken", list);
    }
}
