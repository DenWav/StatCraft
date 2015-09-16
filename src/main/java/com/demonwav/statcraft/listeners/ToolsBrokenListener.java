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
import com.demonwav.statcraft.querydsl.QToolsBroken;
import com.demonwav.statcraft.querydsl.ToolsBroken;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import java.util.UUID;

public class ToolsBrokenListener implements Listener {

    private StatCraft plugin;

    public ToolsBrokenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final short item = (short) event.getBrokenItem().getType().getId();

        plugin.getThreadManager().schedule(ToolsBroken.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QToolsBroken t = QToolsBroken.toolsBroken;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);

                    if (clause == null)
                        return;

                    clause.columns(t.id, t.item, t.amount).values(id, item, 1).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);

                    if (clause == null)
                        return;

                    clause.where(t.id.eq(id), t.item.eq(item)).set(t.amount, t.amount.add(1)).execute();
                }
            }
        });
    }
}
