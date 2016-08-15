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
import com.demonwav.statcraft.commands.CustomResponse;
import com.demonwav.statcraft.commands.ResponseBuilder;
import com.demonwav.statcraft.querydsl.QBlockBreak;

import com.mysema.query.sql.SQLQuery;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SCMined extends SCTemplate implements CustomResponse {

    public SCMined(StatCraft plugin) {
        super(plugin);
        this.plugin.getBaseCommand().registerCommand("mined", this);
    }

    @Override
    public boolean hasPermission(CommandSender sender, String[] args) {
        return sender.hasPermission("statcraft.user.mined");
    }

    @Override
    public String playerStatResponse(String name, List<String> args, Connection connection) {
        return null;
    }

    @Override
    public String serverStatListResponse(int num, List<String> args, Connection connection) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void respondToCommand(final CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("Usage: /sc mined <playername> <material|blockid|blockid:damage> [-all]");
            return;
        }

        if (args.length == 3 && !args[2].equalsIgnoreCase("-all")) {
            sender.sendMessage("Usage: /sc mined <playername> <material|blockid|blockid:damage> [-all]");
            return;
        }

        final String name = args[0];
        final String type = args[1];
        int blockid = 0;
        int damage = 0;
        boolean all = false;

        if (args.length == 3)
            all = true;

        if (type.contains(":")) {
            String[] split = type.split(":");
            if (split.length != 2) {
                sender.sendMessage("Block id must follow format 'blockid' or 'blockid:damage'.");
                return;
            }

            try {
                blockid = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                Material material = Material.getMaterial(split[0].toUpperCase().replaceAll("\\s+", "_"));
                blockid = material.getId();
            }

            try {
                damage = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Damage value must be an integer.");
                return;
            }
        } else {
            try {
                blockid = Integer.parseInt(type);
                damage = 0;
            } catch (NumberFormatException e) {
                Material material = Material.getMaterial(type.toUpperCase().replaceAll("\\s+", "_"));
                if (material != null) {
                    blockid = material.getId();
                    damage = new MaterialData(material).getData();
                }
            }
        }

        final int finalBlockid = blockid;
        final int finalDamage = damage;
        final boolean finalAll = all;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (final Connection connection = plugin.getDatabaseManager().getConnection()) {
                String response;
                try {
                    int id = getId(name);
                    if (id < 0)
                        throw new Exception();

                    SQLQuery query = plugin.getDatabaseManager().getNewQuery(connection);
                    if (query == null)
                        return;
                    QBlockBreak b = QBlockBreak.blockBreak;

                    Integer result;

                    if (finalDamage == -1) {
                        result = query
                            .from(b)
                            .where(
                                b.id.eq(id),
                                b.blockid.eq((short) finalBlockid)
                            ).uniqueResult(b.amount.sum());
                    } else {
                        result = query
                            .from(b)
                            .where(
                                b.id.eq(id),
                                b.blockid.eq((short) finalBlockid),
                                b.damage.eq((short) finalDamage)
                            ).groupBy(b.worldId)
                            .uniqueResult(b.amount.sum());
                    }

                    response = new ResponseBuilder(plugin)
                        .setName(name)
                        .setStatName(WordUtils.capitalizeFully(type) + " mined")
                        .addStat("Total", df.format(result == null ? 0 : result))
                        .toString();
                } catch (Exception e) {
                    response = new ResponseBuilder(plugin)
                        .setName(name)
                        .setStatName(WordUtils.capitalizeFully(type) + " mined")
                        .addStat("Total", df.format(0))
                        .toString();
                }

                if (finalAll)
                    response = ChatColor.valueOf(plugin.getConfig().getColors().getPublicIdentifier())
                        + "@" + sender.getName() + ": " + response;

                final String finalResponse = response;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (finalAll) {
                        plugin.getServer().broadcastMessage(finalResponse);
                    } else {
                        sender.sendMessage(finalResponse);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
