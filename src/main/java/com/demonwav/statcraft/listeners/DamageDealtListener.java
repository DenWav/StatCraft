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
import com.demonwav.statcraft.magic.EntityCode;
import com.demonwav.statcraft.querydsl.DamageDealt;
import com.demonwav.statcraft.querydsl.QDamageDealt;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

                plugin.getThreadManager().schedule(DamageDealt.class, new Runnable() {
                    @Override
                    public void run() {
                        int id = plugin.getDatabaseManager().getPlayerId(uuid);

                        QDamageDealt d = QDamageDealt.damageDealt;

                        // For special entities which are clumped together
                        // currently only skeletons and wither skeletons fall under this category
                        EntityCode code = EntityCode.fromEntity(entity);

                        String entityValue;
                        if (entity instanceof Player) {
                            entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                        } else {
                            entityValue = code.getName(entity.getName());
                        }

                        Util.damage(plugin, d, d.id, d.entity, d.amount, id, entityValue, damageDealt);
                    }
                });
            }
        }
    }
}
