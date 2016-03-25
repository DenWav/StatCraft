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
import com.demonwav.statcraft.magic.EntityCode;
import com.demonwav.statcraft.querydsl.QDamageDealt;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageDealtListener implements Listener {

    private StatCraft plugin;

    public DamageDealtListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager instanceof Player) {
            final UUID uuid = damager.getUniqueId();
            final int damageDealt = (int) Math.round(event.getFinalDamage());

            if (damagee instanceof LivingEntity) {
                final LivingEntity entity = (LivingEntity) event.getEntity();

                plugin.getThreadManager().schedule(
                    QDamageDealt.class, uuid,
                    (d, query, id) -> {
                        Map<String, String> map = new HashMap<>();

                        // For special entities which are clumped together
                        // currently only skeletons and wither skeletons fall under this category
                        EntityCode code = EntityCode.fromEntity(entity);

                        String entityValue;
                        if (entity instanceof Player) {
                            entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                        } else {
                            entityValue = code.getName(entity.getName());
                        }

                        map.put("entityValue", entityValue);
                        return map;
                    }, (d, clause, id, map) ->
                        clause.columns(d.id, d.entity, d.amount)
                            .values(id, map.get("entityValue"), damageDealt).execute(),
                    (d, clause, id, map) ->
                        clause.where(d.id.eq(id), d.entity.eq(map.get("entityValue")))
                            .set(d.amount, d.amount.add(damageDealt)).execute()
                );
            }
        }
    }
}
