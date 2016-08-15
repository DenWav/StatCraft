/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.magic.BucketCode;
import com.demonwav.statcraft.querydsl.QBucketFill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.UUID;

public class BucketFillListener implements Listener {

    private final StatCraft plugin;

    public BucketFillListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final BucketCode code;
        switch (event.getItemStack().getType()) {
            case MILK_BUCKET:
                code = BucketCode.MILK;
                break;
            case LAVA_BUCKET:
                code = BucketCode.LAVA;
                break;
            default: // default to water
                code = BucketCode.WATER;
                break;
        }

        plugin.getThreadManager().schedule(
            QBucketFill.class, uuid, worldName,
            (f, clause, id, worldId) ->
                clause.columns(f.id, f.worldId, f.type, f.amount)
                    .values(id, worldId, code.getCode(), 1).execute(),
            (f, clause, id, worldId) ->
                clause.where(f.id.eq(id), f.worldId.eq(worldId), f.type.eq(code.getCode()))
                    .set(f.amount, f.amount.add(1)).execute()
        );
    }
}
