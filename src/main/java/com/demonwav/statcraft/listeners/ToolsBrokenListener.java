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
import com.demonwav.statcraft.querydsl.QToolsBroken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import java.util.UUID;

public class ToolsBrokenListener implements Listener {

    private final StatCraft plugin;

    public ToolsBrokenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final UUID worldUuid = event.getPlayer().getWorld().getUID();
        final short item = (short) event.getBrokenItem().getType().getId();

        plugin.getThreadManager().schedule(
            QToolsBroken.class, uuid, worldUuid,
            (t, clause, id, worldId) ->
                clause.columns(t.id, t.worldId, t.item, t.amount).values(id, worldId, item, 1).execute(),
            (t, clause, id, worldId) ->
                clause.where(t.id.eq(id), t.worldId.eq(worldId), t.item.eq(item)).set(t.amount, t.amount.add(1)).execute()
        );
    }
}
