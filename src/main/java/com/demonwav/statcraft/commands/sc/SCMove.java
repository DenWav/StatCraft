/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands.sc;

import static com.demonwav.statcraft.querydsl.QMove.move;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.commands.SecondaryArgument;
import com.demonwav.statcraft.magic.MoveCode;
import com.demonwav.statcraft.querydsl.QMove;
import com.demonwav.statcraft.querydsl.QPlayers;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SCMove extends SCTemplate {

    private static List<String> tabComplete = new ArrayList<>(15);

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
                        "flying", "diving", "minecart", "boat", "pig", "horse", "breakdown", "elytra"})
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        String arg = null;
        try {
            int id = getId(name);

            QMove m = move;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
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
                    List<Tuple> list = query.from(m).where(m.id.eq(id)).groupBy(m.vehicle).orderBy(m.distance.sum().desc()).list(m.vehicle, m.distance.sum());

                    StringBuilder sb = new StringBuilder();

                    sb  .append(ChatColor.valueOf(plugin.getConfig().getColors().getStatTitle()))
                        .append("- ")
                        .append(ChatColor.valueOf(plugin.getConfig().getColors().getPlayerName()))
                        .append(name).append(" ")
                        .append(ChatColor.valueOf(plugin.getConfig().getColors().getStatSeparator()))
                        .append("| ")
                        .append(ChatColor.valueOf(plugin.getConfig().getColors().getStatTitle()))
                        .append("Move Breakdown")
                        .append(" -");

                    for (Tuple tuple : list) {
                        Byte vehicle = tuple.get(m.vehicle);
                        if (vehicle == null) {
                            continue;
                        }

                        MoveCode code = MoveCode.fromCode(vehicle);
                        if (code != null) {
                            Integer distance = tuple.get(m.distance.sum());
                            if (distance == null) {
                                continue;
                            }

                            sb.append("\n")
                                .append(ChatColor.valueOf(plugin.getConfig().getColors().getStatLabel()))
                                .append(WordUtils.capitalizeFully(code.name()))
                                .append(": ")
                                .append(ChatColor.valueOf(plugin.getConfig().getColors().getStatValue()))
                                .append(Util.distanceUnits(distance));
                        }
                    }
                    return sb.toString();
                } else {
                    MoveCode code = MoveCode.valueOf(arg.toUpperCase());
                    Integer result = query.from(m).where(m.id.eq(id), m.vehicle.eq(code.getCode())).uniqueResult(m.distance.sum());
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
        "flying", "diving", "minecart", "boat", "pig", "horse", "elytra"})
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        QMove m = move;
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
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
            return tabComplete.stream().filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
        } else {
            return super.onTabComplete(sender, args);
        }
    }

    static {
        tabComplete.add("-all");
        tabComplete.add("-boat");
        tabComplete.add("-breakdown");
        tabComplete.add("-climbing");
        tabComplete.add("-crouching");
        tabComplete.add("-diving");
        tabComplete.add("-elytra");
        tabComplete.add("-falling");
        tabComplete.add("-flying");
        tabComplete.add("-horse");
        tabComplete.add("-minecart");
        tabComplete.add("-pig");
        tabComplete.add("-sprinting");
        tabComplete.add("-swimming");
        tabComplete.add("-walking");
    }
}
