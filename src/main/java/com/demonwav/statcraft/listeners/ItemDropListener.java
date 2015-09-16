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
import com.demonwav.statcraft.querydsl.ItemDrops;
import com.demonwav.statcraft.querydsl.QItemDrops;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.UUID;

public class ItemDropListener implements Listener {

    StatCraft plugin;

    public ItemDropListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final short itemid = (short) event.getItemDrop().getItemStack().getTypeId();
        final short damage = Util.damageValue(itemid, event.getItemDrop().getItemStack().getData().getData());
        final int amount = event.getItemDrop().getItemStack().getAmount();

        plugin.getThreadManager().schedule(ItemDrops.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QItemDrops i = QItemDrops.itemDrops;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(i);

                    if (clause == null)
                        return;

                    clause.columns(i.id, i.item, i.damage, i.amount)
                        .values(id, itemid, damage, amount).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(i);

                    if (clause == null)
                        return;

                    clause.where(
                        i.id.eq(id),
                        i.item.eq(itemid),
                        i.damage.eq(damage)
                    ).set(i.amount, i.amount.add(amount)).execute();
                }
            }
        });
    }
}
