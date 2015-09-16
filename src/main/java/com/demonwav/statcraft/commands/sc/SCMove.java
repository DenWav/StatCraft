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
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.commands.SecondaryArgument;
import com.demonwav.statcraft.magic.MoveCode;
import com.demonwav.statcraft.querydsl.Move;
import com.demonwav.statcraft.querydsl.QMove;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class SCMove extends SCTemplate {

    public SCMove(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("move", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.move");
    }

    @Override
    @SecondaryArgument({"walking", "crouching", "sprinting", "swimming", "falling", "climbing",
                        "flying", "diving", "minecart", "boat", "pig", "horse", "breakdown"})
    public String playerStatResponse(String name, List<String> args) {
        String arg = null;
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);

            QMove m = QMove.move;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery();
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            if (args.size() == 0) {
                Integer result = query.from(m).where(m.id.eq(id)).uniqueResult(m.distance.sum());
                if (result == null)
                    throw new Exception();

                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Move")
                    .addStat("Total", Util.distanceUnits(result))
                    .toString();
            } else {
                arg = args.get(0);
                if (arg.equalsIgnoreCase("breakdown")) {
                    List<Move> list = query.from(m).where(m.id.eq(id)).orderBy(m.distance.desc()).list(m);

                    StringBuilder sb = new StringBuilder();

                    sb  .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                        .append("- ")
                        .append(ChatColor.valueOf(plugin.config().colors.player_name))
                        .append(name).append(" ")
                        .append(ChatColor.valueOf(plugin.config().colors.stat_separator))
                        .append("| ")
                        .append(ChatColor.valueOf(plugin.config().colors.stat_title))
                        .append("Move Breakdown")
                        .append(" -");

                    for (Move move : list) {
                        MoveCode code = MoveCode.fromCode(move.getVehicle());
                        if (code != null)
                        sb  .append("\n")
                            .append(ChatColor.valueOf(plugin.config().colors.stat_label))
                            .append(WordUtils.capitalizeFully(code.name()))
                            .append(": ")
                            .append(ChatColor.valueOf(plugin.config().colors.stat_value))
                            .append(Util.distanceUnits(move.getDistance()));
                    }
                    return sb.toString();
                } else {
                    MoveCode code = MoveCode.valueOf(arg.toUpperCase());
                    Integer result = query.from(m).where(m.id.eq(id), m.vehicle.eq(code.getCode())).uniqueResult(m.distance);
                    if (result == null)
                        throw new Exception();

                    return new ResponseBuilder(plugin)
                        .setName(name)
                        .setStatName("Move")
                        .addStat(WordUtils.capitalizeFully(arg), Util.distanceUnits(result))
                        .toString();
                }

            }
        } catch (Exception e) {
            return new ResponseBuilder(plugin)
                .setName(name)
                .setStatName("Move")
                .addStat(arg == null ? "Total" : WordUtils.capitalizeFully(arg), Util.distanceUnits(0))
                .toString();
        }
    }

    @Override
    @SecondaryArgument({"walking", "crouching", "sprinting", "swimming", "falling", "climbing",
        "flying", "diving", "minecart", "boat", "pig", "horse"})
    public String serverStatListResponse(int num, List<String> args) {
        QMove m = QMove.move;
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";

        List<Tuple> list;
        String arg = null;

        if (args.size() == 0) {
            list = query
                .from(m)
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum());
        } else {
            arg = args.get(0);
            MoveCode code = MoveCode.valueOf(arg.toUpperCase());
            list = query
                .from(m)
                .where(m.vehicle.eq(code.getCode()))
                .leftJoin(p)
                .on(m.id.eq(p.id))
                .groupBy(p.name)
                .orderBy(m.distance.sum().desc())
                .limit(num)
                .list(p.name, m.distance.sum());
        }

        return topListDistanceResponse(arg == null ? "Move" : WordUtils.capitalizeFully(arg), list);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args[args.length -1].startsWith("-")) {
            List<String> result = new LinkedList<>();
            List<String> list = new LinkedList<>();
            list.add("-all");
            list.add("-boat");
            list.add("-breakdown");
            list.add("-climbing");
            list.add("-crouching");
            list.add("-diving");
            list.add("-falling");
            list.add("-flying");
            list.add("-horse");
            list.add("-minecart");
            list.add("-pig");
            list.add("-sprinting");
            list.add("-swimming");
            list.add("-walking");
            for (String s : list) {
                if (s.startsWith(args[args.length -1])) {
                    result.add(s);
                }
            }
            return result;
        } else {
            return super.onTabComplete(sender, args);
        }
    }
}
