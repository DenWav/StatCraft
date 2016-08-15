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
import com.demonwav.statcraft.querydsl.QToolsBroken;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import java.util.UUID;

public class ToolsBrokenListener implements Listener {

    private final StatCraft plugin;

    public ToolsBrokenListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(final PlayerItemBreakEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final short item = (short) event.getBrokenItem().getType().getId();

        plugin.getThreadManager().schedule(
            QToolsBroken.class, uuid, worldName,
            (t, clause, id, worldId) ->
                clause.columns(t.id, t.worldId, t.item, t.amount).values(id, worldId, item, 1).execute(),
            (t, clause, id, worldId) ->
                clause.where(t.id.eq(id), t.worldId.eq(worldId), t.item.eq(item)).set(t.amount, t.amount.add(1)).execute()
        );
    }
}
