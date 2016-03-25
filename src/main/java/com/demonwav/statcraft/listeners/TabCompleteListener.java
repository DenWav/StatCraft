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
import com.demonwav.statcraft.querydsl.QTabComplete;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.UUID;

public class TabCompleteListener implements Listener {

    private StatCraft plugin;

    public TabCompleteListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTabComplete(PlayerChatTabCompleteEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();

        plugin.getThreadManager().schedule(
            QTabComplete.class, uuid,
            (t, clause, id) ->
                clause.columns(t.id, t.amount).values(id, 1).execute(),
            (t, clause, id) ->
                clause.where(t.id.eq(id)).set(t.amount, t.amount.add(1)).execute()
        );
    }
}
