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
import com.demonwav.statcraft.querydsl.QWorldChange;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

public class WorldChangeListener implements Listener {

    private final StatCraft plugin;

    public WorldChangeListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String fromWorldName = event.getFrom().getName();
        final String toWorldName = event.getPlayer().getWorld().getName();

        plugin.getThreadManager().schedule(
            QWorldChange.class, uuid, fromWorldName,
            (w, query, id, worldId) ->
                plugin.getDatabaseManager().getWorldId(toWorldName),
            (w, clause, id, fromWorldId, toWorldId) ->
                clause.columns(w.id, w.toWorld, w.fromWorld, w.amount)
                    .values(id, toWorldId, fromWorldId, 1).execute(),
            (w, clause, id, fromWorldId, toWorldId) ->
                clause.where(
                    w.id.eq(id),
                    w.toWorld.eq(toWorldId),
                    w.fromWorld.eq(fromWorldId)
                ).set(w.amount, w.amount.add(1)).execute()
        );

        plugin.getMoveUpdater().run(event.getPlayer(), fromWorldName);
    }
}
