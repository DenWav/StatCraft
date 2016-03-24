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
import com.demonwav.statcraft.querydsl.QItemPickups;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.UUID;

public class ItemPickUpListener implements Listener {

    private StatCraft plugin;

    public ItemPickUpListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        final short itemid = (short) event.getItem().getItemStack().getTypeId();
        final short damage = Util.damageValue(itemid, event.getItem().getItemStack().getData().getData());
        final UUID uuid = event.getPlayer().getUniqueId();
        final int amount = event.getItem().getItemStack().getAmount();

        plugin.getThreadManager().schedule(
            QItemPickups.class, uuid,
            (i, clause, id) ->
                clause.columns(i.id, i.item, i.damage, i.amount)
                    .values(id, itemid, damage, amount).execute(),
            (i, clause, id) ->
                clause.where(i.id.eq(id), i.item.eq(itemid), i.damage.eq(damage))
                    .set(i.amount, i.amount.add(amount)).execute()
        );
    }
}
