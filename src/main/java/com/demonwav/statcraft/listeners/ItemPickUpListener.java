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
import com.demonwav.statcraft.querydsl.QItemPickups;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.UUID;

public class ItemPickUpListener implements Listener {

    private final StatCraft plugin;

    public ItemPickUpListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        final short itemid = (short) event.getItem().getItemStack().getTypeId();
        final short damage = Util.damageValue(itemid, event.getItem().getItemStack().getData().getData());
        final UUID uuid = event.getPlayer().getUniqueId();
        final UUID worldUuid = event.getPlayer().getWorld().getUID();
        final int amount = event.getItem().getItemStack().getAmount();

        plugin.getThreadManager().schedule(
            QItemPickups.class, uuid, worldUuid,
            (i, clause, id, worldId) ->
                clause.columns(i.id, i.worldId, i.item, i.damage, i.amount)
                    .values(id, worldId, itemid, damage, amount).execute(),
            (i, clause, id, worldId) ->
                clause.where(i.id.eq(id), i.worldId.eq(worldId), i.item.eq(itemid), i.damage.eq(damage))
                    .set(i.amount, i.amount.add(amount)).execute()
        );
    }
}
