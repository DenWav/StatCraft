/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.ServerStatUpdater;
import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.querydsl.Joins;
import com.demonwav.statcraft.querydsl.Jumps;
import com.demonwav.statcraft.querydsl.Move;
import com.demonwav.statcraft.querydsl.PlayTime;
import com.demonwav.statcraft.querydsl.Players;
import com.demonwav.statcraft.querydsl.QJoins;
import com.demonwav.statcraft.querydsl.QPlayTime;
import com.demonwav.statcraft.querydsl.QSeen;
import com.demonwav.statcraft.querydsl.Seen;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayTimeListener implements Listener {

    private StatCraft plugin;

    public PlayTimeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getThreadManager().schedule(Players.class, new Runnable() {
            @Override
            public void run() {
                // This MUST be done before the other two jobs
                final int id = plugin.setupPlayer(event.getPlayer());
                plugin.players.put(name, uuid);

                if (plugin.config().stats.joins) {
                    plugin.getThreadManager().schedule(Joins.class, new Runnable() {
                        @Override
                        public void run() {
                            QJoins j = QJoins.joins;

                            try {
                                // INSERT
                                SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(j);

                                if (clause == null)
                                    return;

                                clause.columns(j.id, j.amount).values(id, 1).execute();
                            } catch (QueryException e) {
                                // UPDATE
                                SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(j);

                                if (clause == null)
                                    return;

                                clause.where(j.id.eq(id)).set(j.amount, j.amount.add(1)).execute();
                            }
                        }
                    });
                }

                plugin.getThreadManager().schedule(Seen.class, new Runnable() {
                    @Override
                    public void run() {
                        QSeen s = QSeen.seen;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(s);

                            if (clause == null)
                                return;

                            clause.columns(s.id, s.lastJoinTime).values(id, currentTime).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(s);

                            if (clause == null)
                                return;

                            clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute();
                        }
                    }
                });

                plugin.getThreadManager().schedule(Move.class, new ServerStatUpdater.Move(plugin));
                plugin.getThreadManager().schedule(Jumps.class, new ServerStatUpdater.Jump(plugin));
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getThreadManager().schedule(Seen.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QSeen s = QSeen.seen;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(s);

                    if (clause == null)
                        return;

                    clause.columns(s.id, s.lastLeaveTime).values(id, currentTime).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(s);

                    if (clause == null)
                        return;

                    clause.where(s.id.eq(id)).set(s.lastLeaveTime, currentTime).execute();
                }
            }
        });

        final int currentPlayTime = (int) Math.round(event.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) * 0.052);

        plugin.getThreadManager().schedule(PlayTime.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QPlayTime p = QPlayTime.playTime;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(p);

                    if (clause == null)
                        return;

                    clause.columns(p.id, p.amount).values(id, currentPlayTime).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(p);

                    if (clause == null)
                        return;

                    clause.where(p.id.eq(id)).set(p.amount, currentPlayTime).execute();
                }
            }
        });
    }
}
