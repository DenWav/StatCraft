/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.querydsl.QJumps;

import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import java.util.UUID;

public class JumpListener implements Listener {

    private final StatCraft plugin;

    public JumpListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJump(final PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() == Statistic.JUMP) {
            final UUID uuid = event.getPlayer().getUniqueId();
            final String worldName = event.getPlayer().getWorld().getName();

            plugin.getThreadManager().schedule(
                QJumps.class, uuid, worldName,
                (j, clause, id, worldId) ->
                    clause.columns(j.id, j.worldId, j.amount).values(id, worldId, 1).execute(),
                (j, clause, id, worldId) ->
                    clause.where(j.id.eq(id), j.worldId.eq(worldId)).set(j.amount, j.amount.add(1)).execute()
            );
        }
    }
}
