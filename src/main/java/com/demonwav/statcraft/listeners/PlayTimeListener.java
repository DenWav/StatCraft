/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.ServerStatUpdater;
import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.querydsl.QJoins;
import com.demonwav.statcraft.querydsl.QPlayTime;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QSeen;

import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayTimeListener implements Listener {

    private final StatCraft plugin;

    public PlayTimeListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getThreadManager().scheduleRaw(
            QPlayers.class,
            (conn) -> {
                // This MUST be done before the other two jobs
                final int id = plugin.setupPlayer(event.getPlayer(), conn);
                final int worldId = plugin.getDatabaseManager().getWorldId(worldName);
                plugin.players.put(name, uuid);

                if (plugin.config().getStats().isJoins()) {
                    plugin.getThreadManager().scheduleRaw(
                        QJoins.class,
                        (connection) ->
                            Util.runQuery(
                                QJoins.class,
                                (j, clause) ->
                                    clause.columns(j.id, j.worldId, j.amount).values(id, worldId, 1).execute(),
                                (j, clause) ->
                                    clause.where(j.id.eq(id), j.worldId.eq(worldId)).set(j.amount, j.amount.add(1)).execute(),
                                connection,
                                plugin
                            )
                    );
                }

                plugin.getThreadManager().scheduleRaw(
                    QSeen.class,
                    (connection) ->
                        Util.runQuery(
                            QSeen.class,
                            (s, clause) ->
                                clause.columns(s.id, s.lastJoinTime).values(id, currentTime).execute(),
                            (s, clause) ->
                                clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute(),
                            connection,
                            plugin
                        )
                );
            }
        );

        new ServerStatUpdater.Move(plugin).run();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getThreadManager().schedule(
            QSeen.class, uuid, worldName,
            (s, clause, id, worldId) ->
                clause.columns(s.id, s.lastLeaveTime).values(id, currentTime).execute(),
            (s, clause, id, worldId) ->
                clause.where(s.id.eq(id)).set(s.lastLeaveTime, currentTime).execute()
        );

        final int currentPlayTime = (int) Math.round(event.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) * 0.052);

        plugin.getThreadManager().schedule(
            QPlayTime.class, uuid, worldName,
            (p, clause, id, worldId) ->
                clause.columns(p.id, p.amount).values(id, currentPlayTime).execute(),
            (p, clause, id, worldId) ->
                clause.where(p.id.eq(id)).set(p.amount, currentPlayTime).execute()
        );

        plugin.getMoveUpdater().run(event.getPlayer());
    }
}
