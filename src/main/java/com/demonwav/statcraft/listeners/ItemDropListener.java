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
import com.demonwav.statcraft.Util;
import com.demonwav.statcraft.querydsl.QItemDrops;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.UUID;

public class ItemDropListener implements Listener {

    private final StatCraft plugin;

    public ItemDropListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final short itemid = (short) event.getItemDrop().getItemStack().getTypeId();
        final short damage = Util.damageValue(itemid, event.getItemDrop().getItemStack().getData().getData());
        final int amount = event.getItemDrop().getItemStack().getAmount();

        plugin.getThreadManager().schedule(
            QItemDrops.class, uuid, worldName,
            (i, clause, id, worldId) ->
                clause.columns(i.id, i.worldId, i.item, i.damage, i.amount)
                    .values(id, worldId, itemid, damage, amount).execute(),
            (i, clause, id, worldId) ->
                clause.where(i.id.eq(id), i.worldId.eq(worldId), i.item.eq(itemid), i.damage.eq(damage))
                    .set(i.amount, i.amount.add(amount)).execute()
        );
    }
}
