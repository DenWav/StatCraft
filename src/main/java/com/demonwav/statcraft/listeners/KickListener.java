/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.querydsl.QKicks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;

public class KickListener implements Listener {

    private StatCraft plugin;

    public KickListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        final UUID uuid= event.getPlayer().getUniqueId();
        final String reason = event.getReason();

        plugin.getThreadManager().schedule(
            QKicks.class, uuid,
            (k, clause, id) ->
                clause.columns(k.id, k.reason, k.amount).values(id, reason, 1).execute(),
            (k, clause, id) ->
                clause.where(k.id.eq(id), k.reason.eq(reason)).set(k.amount, k.amount.add(1)).execute()
        );
    }

}
