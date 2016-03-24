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
import com.demonwav.statcraft.querydsl.QSleep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SleepyTimeListener implements Listener {

    private StatCraft plugin;

    public SleepyTimeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(
            QSleep.class, uuid,
            (s, clause, id) ->
                clause.columns(s.id, s.enterBed).values(id, currentTime).execute(),
            (s, clause, id) ->
                clause.where(s.id.eq(id)).set(s.enterBed, currentTime).execute()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(
            QSleep.class, uuid,
            (l, clause, id) ->
                clause.columns(l.id, l.leaveBed).values(id, currentTime).execute(),
            (l, clause, id) ->
                clause.where(l.id.eq(id)).set(l.leaveBed, currentTime).execute()
        );

        plugin.getThreadManager().schedule(
            QSleep.class, uuid,
            (s, query, id) -> {
                Map<String, Integer> map = new HashMap<>();

                Integer enterBed = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed);
                enterBed = enterBed == null ? 0 : enterBed;

                map.put("timeSlept", currentTime - enterBed);
                if (enterBed == 0) {
                    map.put("change", 0);
                } else {
                    map.put("change", 1);
                }

                return map;
            },
            (s, clause, id, map) -> {
                if (map.get("change") != 0) {
                    clause.columns(s.id, s.timeSlept).values(id, map.get("timeSlept")).execute();
                }
            }, (s, clause, id, map) -> {
                if (map.get("change") != 0) {
                    clause.where(s.id.eq(id)).set(s.timeSlept, s.timeSlept.add(map.get("timeSlept"))).execute();
                }
            }
        );
    }
}
