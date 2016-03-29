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
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.querydsl.QBlockBreak;
import com.demonwav.statcraft.querydsl.QBlockPlace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class BlockListener implements Listener {

    private final StatCraft plugin;

    public BlockListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final short blockid = (short) event.getBlock().getTypeId();
        final short damage = Util.damageValue(blockid, event.getBlock().getData());
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();

        plugin.getThreadManager().schedule(
            QBlockBreak.class, uuid, worldName,
            (b, clause, id, worldId) ->
                clause.columns(b.id, b.worldId, b.blockid, b.damage, b.amount)
                    .values(id, worldId, blockid, damage, 1).execute(),
            (b, clause, id, worldId) ->
                clause.where(b.id.eq(id), b.worldId.eq(worldId), b.blockid.eq(blockid), b.damage.eq(damage))
                    .set(b.amount, b.amount.add(1)).execute()
        );
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final short blockid = (short) event.getBlock().getTypeId();
        final short damage = Util.damageValue(blockid, event.getBlock().getData());
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();

        plugin.getThreadManager().schedule(
            QBlockPlace.class, uuid, worldName,
            (b, clause, id, worldId) ->
                clause.columns(b.id, b.worldId, b.blockid, b.damage, b.amount)
                    .values(id, worldId, blockid, damage, 1).execute(),
            (b, clause, id, worldId) ->
                clause.where(b.id.eq(id), b.worldId.eq(worldId), b.blockid.eq(blockid), b.damage.eq(damage))
                    .set(b.amount, b.amount.add(1)).execute()
        );
    }
}
