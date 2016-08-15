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
import com.demonwav.statcraft.querydsl.QHighestLevel;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.UUID;

public class HighestLevelListener implements Listener {

    private final StatCraft plugin;

    public HighestLevelListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevel(final PlayerLevelChangeEvent event) {
        final int newLevel = event.getNewLevel();
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();

        plugin.getThreadManager().schedule(
            QHighestLevel.class, uuid, worldName,
            (h, clause, id, worldId) ->
                clause.columns(h.id, h.level)
                    .values(id, newLevel).execute(),
            (h, clause, id, worldId) ->
                clause.where(h.id.eq(id), h.level.lt(newLevel)).set(h.level, newLevel).execute()
        );
    }
}
