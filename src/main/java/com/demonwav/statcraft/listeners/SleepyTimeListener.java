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
import com.demonwav.statcraft.querydsl.QSleep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.UUID;

public class SleepyTimeListener implements Listener {

    private final StatCraft plugin;

    public SleepyTimeListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(final PlayerBedEnterEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(
            QSleep.class, uuid, worldName,
            (s, clause, id, worldId) ->
                clause.columns(s.id, s.worldId, s.enterBed).values(id, worldId, currentTime).execute(),
            (s, clause, id, worldId) ->
                clause.where(s.id.eq(id), s.worldId.eq(worldId)).set(s.enterBed, currentTime).execute()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(final PlayerBedLeaveEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(
            QSleep.class, uuid, worldName,
            (l, clause, id, worldId) ->
                clause.columns(l.id, l.worldId, l.leaveBed).values(id, worldId, currentTime).execute(),
            (l, clause, id, worldId) ->
                clause.where(l.id.eq(id), l.worldId.eq(worldId)).set(l.leaveBed, currentTime).execute()
        );

        plugin.getThreadManager().schedule(
            QSleep.class, uuid, worldName,
            (s, query, id, worldId) -> {
                Integer enterBed = query.from(s).where(s.id.eq(id), s.worldId.eq(worldId)).uniqueResult(s.enterBed);
                enterBed = enterBed == null ? 0 : enterBed;

                return currentTime - enterBed;
            },
            (s, clause, id, worldId, timeSlept) -> {
                if (timeSlept != currentTime) {
                    clause.columns(s.id, s.timeSlept).values(id, timeSlept).execute();
                }
            }, (s, clause, id, worldId, timeSlept) -> {
                if (timeSlept != currentTime) {
                    clause.where(s.id.eq(id), s.worldId.eq(worldId)).set(s.timeSlept, s.timeSlept.add(timeSlept)).execute();
                }
            }
        );
    }
}
