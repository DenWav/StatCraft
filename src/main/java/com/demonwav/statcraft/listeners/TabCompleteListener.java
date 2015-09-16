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
import com.demonwav.statcraft.querydsl.QTabComplete;
import com.demonwav.statcraft.querydsl.TabComplete;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
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

        plugin.getThreadManager().schedule(TabComplete.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QTabComplete t = QTabComplete.tabComplete;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);

                    if (clause == null)
                        return;

                    clause.columns(t.id, t.amount).values(id, 1).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);

                    if (clause == null)
                        return;

                    clause.where(t.id.eq(id)).set(t.amount, t.amount.add(1)).execute();
                }
            }
        });
    }
}
