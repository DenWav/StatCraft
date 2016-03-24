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
import com.demonwav.statcraft.magic.EntityCode;
import com.demonwav.statcraft.querydsl.QKills;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillListener implements Listener {

    private StatCraft plugin;

    public KillListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            final UUID uuid = event.getEntity().getKiller().getUniqueId();
            final LivingEntity entity = event.getEntity();
            final EntityCode code = EntityCode.fromEntity(event.getEntity());

            plugin.getThreadManager().schedule(
                QKills.class, uuid,
                (k, query, id) -> {
                    Map<String, String> map = new HashMap<>();

                    String entityValue;
                    if (entity instanceof Player) {
                        entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                    } else {
                        if (entity instanceof EnderPearl) {
                            entityValue = "Ender Pearl";
                        } else {
                            entityValue = code.getName(entity.getName());
                        }
                    }

                    map.put("entityValue", entityValue);
                    return map;
                }, (k, clause, id, map) ->
                    clause.columns(k.id, k.entity, k.amount)
                        .values(id, map.get("entityValue"), 1).execute(),
                (k, clause, id, map) ->
                    clause.where(
                        k.id.eq(id),
                        k.entity.eq(map.get("entityValue"))
                    ).set(k.amount, k.amount.add(1)).execute()
            );
        }
    }
}
