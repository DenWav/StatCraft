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
import com.demonwav.statcraft.magic.ProjectilesCode;
import com.demonwav.statcraft.querydsl.Projectiles;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QProjectiles;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.path.NumberPath;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SCEggsThrown extends SCTemplate {

    public SCEggsThrown(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("eggsthrown", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.eggsthrown");
    }

    @Override
    @SecondaryArgument({"distance", "farthest"})
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        int total;
        int hatched = 0;
        int notHatched = 0;

        int unHatchedDistance = 0;
        int hatchedDistance = 0;

        int unHatchedMaxThrow = 0;
        int hatchedMaxThrow = 0;
        try {
            int id = plugin.getDatabaseManager().getPlayerId(name);
            if (id < 0)
                throw new Exception();

            QProjectiles p = QProjectiles.projectiles;
            SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
            if (query == null)
                return "Sorry, there seems to be an issue connecting to the database right now.";

            List<Projectiles> list = query.from(p).where(p.id.eq(id), p.type.eq(ProjectilesCode.HATCHED_EGG.getCode())
                .or(p.type.eq(ProjectilesCode.UNHATCHED_EGG.getCode()))).list(p);

            for (Projectiles projectiles : list) {
                ProjectilesCode code = ProjectilesCode.fromCode(projectiles.getType());

                if (code == null)
                    continue;

                switch (code) {
                    case HATCHED_EGG:
                        hatched = projectiles.getAmount();
                        hatchedDistance = projectiles.getTotalDistance();
                        hatchedMaxThrow = projectiles.getMaxThrow();
                        break;
                    case UNHATCHED_EGG:
                        notHatched = projectiles.getAmount();
                        unHatchedDistance = projectiles.getTotalDistance();
                        unHatchedMaxThrow = projectiles.getMaxThrow();
                        break;
                }
            }

            total = hatched + notHatched;
        } catch (Exception ex) {
            total = 0;
            hatched = 0;
            notHatched = 0;
            unHatchedDistance = 0;
            hatchedDistance = 0;
            unHatchedMaxThrow = 0;
            hatchedMaxThrow = 0;
        }

        String arg;
        if (args.size() > 0)
            arg = args.get(0);
        else
            arg = "";

        switch (arg) {
            case "distance":
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Eggs Thrown Total Distance")
                    .addStat("Total", Util.distanceUnits(hatchedDistance + unHatchedDistance))
                    .addStat("Hatched", Util.distanceUnits(hatchedDistance))
                    .addStat("Not Hatched", Util.distanceUnits(unHatchedDistance))
                    .toString();
            case "farthest":
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Eggs Thrown Farthest Throw")
                    .addStat("Hatched", Util.distanceUnits(hatchedMaxThrow))
                    .addStat("Not Hatched", Util.distanceUnits(unHatchedMaxThrow))
                    .toString();
            default:
                return new ResponseBuilder(plugin)
                    .setName(name)
                    .setStatName("Eggs Thrown")
                    .addStat("Total", df.format(total))
                    .addStat("Hatched", df.format(hatched))
                    .addStat("Not Hatched", df.format(notHatched))
                    .toString();
        }
    }

    @Override
    @SecondaryArgument({"distance", "farthest", "unhatched"})
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        boolean distance = false;
        QProjectiles p = QProjectiles.projectiles;
        QPlayers pl = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
        if (query == null)
            return "Sorry, there seems to be an issue connecting to the database right now.";

        NumberPath<Integer> path = null;
        String titlePrefix = "";
        String titlePostfix = "";
        String title;

        ProjectilesCode code = ProjectilesCode.HATCHED_EGG;

        for (String arg : args) {
            switch (arg) {
                case "distance":
                    path = p.totalDistance;
                    titlePrefix = "Total Distance Thrown - ";
                    distance = true;
                    break;
                case "farthest":
                    path = p.maxThrow;
                    titlePrefix = "Farthest ";
                    titlePostfix = " Thrown";
                    distance = true;
                    break;
                case "unhatched":
                    code = ProjectilesCode.UNHATCHED_EGG;
                    break;
            }
        }

        if (path == null) {
            path = p.amount;
            titlePostfix = "s Thrown";
        }

        title = titlePrefix + (code == ProjectilesCode.HATCHED_EGG ? "Hatched Egg" : "Not Hatched Egg") + titlePostfix;

        List<Tuple> list = query
            .from(p)
            .leftJoin(pl)
            .on(p.id.eq(pl.id))
            .where(p.type.eq(code.getCode()))
            .groupBy(pl.name)
            .orderBy(path.desc())
            .limit(num)
            .list(pl.name, path);

        if (distance)
            return topListDistanceResponse(title, list);
        else
            return topListResponse(title, list);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args[args.length -1].startsWith("-")) {
            boolean top = false;
            for (String s : args) {
                if (s.startsWith("-top"))
                    top = true;
            }
            List<String> list = new LinkedList<>();
            list.add("-all");
            list.add("-distance");
            list.add("-farthest");
            if (top)
                list.add("-unhatched");

            return list.stream().filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
        } else {
            return super.onTabComplete(sender, args);
        }
    }
}
