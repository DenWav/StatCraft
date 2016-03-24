/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.magic.MoveCode;
import com.demonwav.statcraft.querydsl.QJumps;
import com.demonwav.statcraft.querydsl.QMove;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ServerStatUpdater {

    public static class Move implements Runnable {

        private StatCraft plugin;

        public Move(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (final MoveCode code : MoveCode.values()) {
                    Statistic stat = code.getStat();
                    final int value = player.getStatistic(stat);
                    final UUID uuid = player.getUniqueId();

                    plugin.getThreadManager().schedule(
                        QMove.class, uuid,
                        (m, clause, id) ->
                            clause.columns(m.id, m.vehicle, m.distance).values(id, code.getCode(), value).execute(),
                        (m, clause, id) ->
                            clause.where(m.id.eq(id), m.vehicle.eq(code.getCode())).set(m.distance, value).execute()
                    );
                }
            }
        }
    }

    public static class Jump implements Runnable {

        private StatCraft plugin;

        public Jump(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                final int jumps = player.getStatistic(Statistic.JUMP);
                final UUID uuid = player.getUniqueId();

                plugin.getThreadManager().schedule(
                    QJumps.class, uuid,
                    (j, clause, id) ->
                        clause.columns(j.id, j.amount).values(id, jumps).execute(),
                    (j, clause, id) ->
                        clause.where(j.id.eq(id)).set(j.amount, jumps).execute()
                );
            }
        }
    }
}
