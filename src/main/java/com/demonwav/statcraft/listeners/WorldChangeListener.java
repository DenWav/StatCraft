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
import com.demonwav.statcraft.querydsl.QWorldChange;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

public class WorldChangeListener implements Listener {

    private StatCraft plugin;

    public WorldChangeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String fromWorld = event.getFrom().getName();
        final String toWorld = event.getPlayer().getWorld().getName();

        plugin.getThreadManager().schedule(
            QWorldChange.class, uuid,
            (w, clause, id) ->
                clause.columns(w.id, w.toWorld, w.fromWorld, w.amount)
                    .values(id, toWorld, fromWorld, 1).execute(),
            (w, clause, id) ->
                clause.where(
                    w.id.eq(id),
                    w.toWorld.eq(toWorld),
                    w.fromWorld.eq(fromWorld)
                ).set(w.amount, w.amount.add(1)).execute()
        );
    }
}
