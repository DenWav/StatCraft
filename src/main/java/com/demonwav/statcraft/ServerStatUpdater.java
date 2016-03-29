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
import com.demonwav.statcraft.querydsl.QMove;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ServerStatUpdater {

    public static final class Move implements Runnable {

        private final StatCraft plugin;

        public Move(StatCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public final void run() {
            plugin.getServer().getOnlinePlayers().forEach(this::run);
        }

        public final void run(final Player player) {
            run(player, player.getWorld().getName());
        }

        public final void run(final Player player, final String worldName) {
            for (final MoveCode code : MoveCode.values()) {
                Statistic stat = code.getStat();
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
}
