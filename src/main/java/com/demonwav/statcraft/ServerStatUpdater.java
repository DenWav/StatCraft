/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.magic.MoveCode;
import com.demonwav.statcraft.magic.PotionEffectCode;
import com.demonwav.statcraft.querydsl.QMove;
import com.demonwav.statcraft.querydsl.QPotioned;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerStatUpdater {

    public static class Move implements Runnable {

        private final StatCraft plugin;

        public Move(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            plugin.getServer().getOnlinePlayers().forEach(this::run);
        }

        public void run(final Player player) {
            run(player, player.getWorld().getName());
        }

        public void run(final Player player, final String worldName) {
            for (final MoveCode code : MoveCode.values()) {
                final Statistic stat = code.getStat();
                final int value = player.getStatistic(stat);
                final UUID uuid = player.getUniqueId();

                plugin.getThreadManager().schedule(
                    QMove.class, uuid, worldName,
                    (m, query, id, worldId) ->
                        get(query.from(m).where(m.id.eq(id), m.vehicle.eq(code.getCode())).uniqueResult(m.distance.sum())),
                    (m, clause, id, worldId, currentTotal) ->
                        clause.columns(m.id, m.worldId, m.vehicle, m.distance)
                            .values(id, worldId, code.getCode(), value - currentTotal).execute(),
                    (m, clause, id, worldId, currentTotal) ->
                        clause.where(m.id.eq(id), m.worldId.eq(worldId), m.vehicle.eq(code.getCode()))
                            .set(m.distance, m.distance.add(value - currentTotal)).execute()
                );
            }
        }

        private static int get(Integer i) {
            return i == null ? 0 : i;
        }
    }

    public static class PotionEffect implements Runnable {

        private final StatCraft plugin;

        public PotionEffect(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            plugin.getServer().getOnlinePlayers().forEach(this::run);
        }

        public void run(final Player player) {
            final List<PotionEffectCode> potionEffects = player.getActivePotionEffects().stream().map(e -> PotionEffectCode.fromEffect(e.getType())).collect(Collectors.toList());
            final UUID uuid = player.getUniqueId();
            final String worldName = player.getWorld().getName();

            plugin.getThreadManager().schedule(
                QPotioned.class, uuid, worldName,
                (p, query, id, worldId) -> {
                    return new Object();
                }, (p, clause, id, worldId, o) -> {

                }, (p, clause, id, worldId, o) -> {

                }
            );
        }
    }
}
