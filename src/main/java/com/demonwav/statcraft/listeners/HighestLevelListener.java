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
import com.demonwav.statcraft.querydsl.HighestLevel;
import com.demonwav.statcraft.querydsl.QHighestLevel;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.UUID;

public class HighestLevelListener implements Listener {

    private StatCraft plugin;

    public HighestLevelListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevel(PlayerLevelChangeEvent event) {
        final int newLevel = event.getNewLevel();
        final UUID uuid = event.getPlayer().getUniqueId();

        plugin.getThreadManager().schedule(HighestLevel.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QHighestLevel h = QHighestLevel.highestLevel;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(h);

                    if (clause == null)
                        return;

                    clause.columns(h.id, h.level)
                        .values(id, newLevel).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(h);

                    if (clause == null)
                        return;

                    clause.where(h.id.eq(id), h.level.lt(newLevel)).set(h.level, newLevel).execute();
                }
            }
        });
    }
}
