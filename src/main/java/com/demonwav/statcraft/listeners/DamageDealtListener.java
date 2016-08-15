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
import com.demonwav.statcraft.magic.EntityCode;
import com.demonwav.statcraft.querydsl.QDamageDealt;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class DamageDealtListener implements Listener {

    private final StatCraft plugin;

    public DamageDealtListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageDealt(final EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damagee = event.getEntity();
        if (damager instanceof Player) {
            final UUID uuid = damager.getUniqueId();
            final String worldName = damager.getWorld().getName();
            final int damageDealt = (int) Math.round(event.getFinalDamage());

            if (damagee instanceof LivingEntity) {
                final LivingEntity entity = (LivingEntity) event.getEntity();

                plugin.getThreadManager().schedule(
                    QDamageDealt.class, uuid, worldName,
                    (d, query, id, worldId) -> {
                        // For special entities which are clumped together
                        // currently only skeletons and wither skeletons fall under this category
                        EntityCode code = EntityCode.fromEntity(entity);

                        String entityValue;
                        if (entity instanceof Player) {
                            entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                        } else {
                            entityValue = code.getName(entity.getName());
                        }

                        return entityValue;
                    }, (d, clause, id, worldId, entityValue) ->
                        clause.columns(d.id, d.worldId, d.entity, d.amount)
                            .values(id, worldId, entityValue, damageDealt).execute(),
                    (d, clause, id, worldId, entityValue) ->
                        clause.where(d.id.eq(id), d.worldId.eq(worldId), d.entity.eq(entityValue))
                            .set(d.amount, d.amount.add(damageDealt)).execute()
                );
            }
        }
    }
}
