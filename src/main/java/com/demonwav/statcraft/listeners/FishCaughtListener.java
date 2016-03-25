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
import com.demonwav.statcraft.magic.FishCode;
import com.demonwav.statcraft.querydsl.QFishCaught;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.UUID;

public class FishCaughtListener implements Listener {

    private StatCraft plugin;

    public FishCaughtListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            final UUID uuid = event.getPlayer().getUniqueId();
            // shouldn't be an issue, but just to protect against a ClassCastException
            if (event.getCaught() instanceof Item) {
                Item item = (Item) event.getCaught();
                final short itemId = (short) item.getItemStack().getTypeId();
                final short damage = Util.damageValue(itemId, item.getItemStack().getData().getData());

                final FishCode code;

                switch (item.getItemStack().getType()) {
                    case RAW_FISH:
                        code = FishCode.FISH;
                        break;
                    case BOW:
                    case ENCHANTED_BOOK:
                    case NAME_TAG:
                    case SADDLE:
                    case WATER_LILY:
                        code = FishCode.TREASURE;
                        break;
                    case BOWL:
                    case LEATHER:
                    case LEATHER_BOOTS:
                    case ROTTEN_FLESH:
                    case STICK:
                    case STRING:
                    case POTION:
                    case BONE:
                    case INK_SACK:
                    case TRIPWIRE_HOOK:
                        code = FishCode.JUNK;
                        break;
                    case FISHING_ROD:
                        if (item.getItemStack().getEnchantments().size() == 0)
                            code = FishCode.JUNK;
                        else
                            code = FishCode.TREASURE;
                        break;
                    default:
                        // Default to junk, though it should never fall under this category
                        code = FishCode.JUNK;
                        break;
                }

                plugin.getThreadManager().schedule(
                    QFishCaught.class, uuid,
                    (f, clause, id) ->
                        clause.columns(f.id, f.item, f.damage, f.type, f.amount)
                            .values(id, itemId, damage, code.getCode(), 1).execute(),
                    (f, clause, id) ->
                        clause.where(f.id.eq(id), f.item.eq(itemId), f.damage.eq(damage), f.type.eq(code.getCode()))
                            .set(f.amount, f.amount.add(1)).execute()
                );
            }
        }
    }
}
