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
import com.demonwav.statcraft.querydsl.QXpGained;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.UUID;

public class XpGainedListener implements Listener {

    private final StatCraft plugin;

    public XpGainedListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXpGain(final PlayerExpChangeEvent event) {
        final int amount = event.getAmount();
        if (amount > 0) {
            final UUID uuid = event.getPlayer().getUniqueId();
            final String worldName = event.getPlayer().getWorld().getName();

            plugin.getThreadManager().schedule(
                QXpGained.class, uuid, worldName,
                (x, clause, id, worldId) ->
                    clause.columns(x.id, x.amount).values(id, amount).execute(),
                (x, clause, id, worldId) ->
                    clause.where(x.id.eq(id)).set(x.amount, x.amount.add(amount)).execute()
            );
        }
    }
}
