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
import com.demonwav.statcraft.querydsl.QDeath;
import com.demonwav.statcraft.querydsl.QDeathByCause;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DeathListener implements Listener {

    private final StatCraft plugin;

    public DeathListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(final PlayerDeathEvent event) {

        final String message = event.getDeathMessage();
        final UUID uuid = event.getEntity().getUniqueId();
        final String worldName = event.getEntity().getWorld().getName();
        String cause;

        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            Entity killer = damageByEntityEvent.getDamager();
            EntityCode code = EntityCode.fromEntity(killer);
            if (killer instanceof Player) {
                cause = String.valueOf(plugin.getDatabaseManager().getPlayerId(killer.getUniqueId()));
            } else {
                if (killer instanceof EnderPearl) {
                    cause = "EnderPearl";
                } else if (killer instanceof WitherSkull) {
                    cause = "WitherSkull";
                } else {
                    cause = code.getName(killer.getName());
                }
            }
        } else {
            EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
            cause = damageCause.name();
        }

        plugin.getThreadManager().schedule(
            QDeath.class, uuid, worldName,
            (d, clause, id, worldId) ->
                clause.columns(d.id, d.message, d.worldId, d.amount).values(id, message, worldId, 1).execute(),
            (d, clause, id, worldId) ->
                clause.where(d.id.eq(id), d.message.eq(message), d.worldId.eq(worldId))
                    .set(d.amount, d.amount.add(1)).execute()
        );

        plugin.getThreadManager().schedule(
            QDeathByCause.class, uuid, worldName,
            (c, clause, id, worldId) ->
                clause.columns(c.id, c.cause, c.worldId, c.amount)
                    .values(id, cause, worldId, 1).execute(),
            (c, clause, id, worldId) ->
                clause.where(c.id.eq(id), c.cause.eq(cause), c.worldId.eq(worldId))
                    .set(c.amount, c.amount.add(1)).execute()
        );
    }
}
