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
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.magic.BucketCode;
import com.demonwav.statcraft.querydsl.BucketEmpty;
import com.demonwav.statcraft.querydsl.QBucketEmpty;

import com.mysema.query.QueryException;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.path.NumberPath;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.UUID;

public class BucketEmptyListener implements Listener {

    private StatCraft plugin;

    public BucketEmptyListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final BucketCode code;
        if (event.getBucket() == Material.LAVA_BUCKET)
            code = BucketCode.LAVA;
        else // default to water
            code = BucketCode.WATER;

        plugin.getThreadManager().schedule(BucketEmpty.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QBucketEmpty e = QBucketEmpty.bucketEmpty;

                Util.bucket(plugin, e, e.id, e.type, e.amount, id, code);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            final UUID uuid = event.getPlayer().getUniqueId();
            final BucketCode code = BucketCode.MILK;

            plugin.getThreadManager().schedule(BucketEmpty.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QBucketEmpty e = QBucketEmpty.bucketEmpty;

                    Util.bucket(plugin, e, e.id, e.type, e.amount, id, code);
                }
            });
        }
    }
}
