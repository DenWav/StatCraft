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

    private StatCraft plugin;

    public XpGainedListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXpGain(PlayerExpChangeEvent event) {
        final int amount = event.getAmount();
        if (amount > 0) {
            final UUID uuid = event.getPlayer().getUniqueId();

            plugin.getThreadManager().schedule(
                QXpGained.class, uuid,
                (x, clause, id) ->
                    clause.columns(x.id, x.amount).values(id, amount).execute(),
                (x, clause, id) ->
                    clause.where(x.id.eq(id)).set(x.amount, x.amount.add(amount)).execute()
            );
        }
    }
}
