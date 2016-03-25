/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
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

    private StatCraft plugin;

    public JumpListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJump(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() == Statistic.JUMP) {
            final UUID uuid = event.getPlayer().getUniqueId();
            final String world = event.getPlayer().getWorld().getName();

            plugin.getThreadManager().schedule(
                QJumps.class, uuid,
                (j, clause, id) ->
                    clause.columns(j.id, j.world, j.amount).values(id, world, 1).execute(),
                (j, clause, id) ->
                    clause.where(j.id.eq(id), j.world.eq(world)).set(j.amount, j.amount.add(1)).execute()
            );
        }
    }
}
