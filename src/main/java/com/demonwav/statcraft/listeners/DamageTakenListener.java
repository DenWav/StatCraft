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
import com.demonwav.statcraft.querydsl.QDamageTaken;

import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class DamageTakenListener implements Listener {

    private final StatCraft plugin;

    public DamageTakenListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageTaken(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            final UUID uuid = event.getEntity().getUniqueId();
            final String worldName = event.getEntity().getWorld().getName();
            final int damageTaken = (int) Math.round(event.getFinalDamage());

            plugin.getThreadManager().schedule(
                QDamageTaken.class, uuid, worldName,
                (t, clause, id, worldId) ->
                    clause.columns(t.id, t.worldId, t.entity, t.amount)
                        .values(id, worldId, event.getCause().name(), damageTaken).execute(),
                (t, clause, id, worldId) ->
                    clause.where(t.id.eq(id), t.worldId.eq(worldId), t.entity.eq(event.getCause().name()))
                        .set(t.amount, t.amount.add(damageTaken)).execute()
            );

            // DROWN ANNOUNCE
            if (plugin.config().getStats().isDrowningAnnounce())
            if (event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastDrownTime(uuid) > 120) {

                    event.getEntity().getServer().broadcastMessage(
                            ChatColor.BLUE +
                            plugin.config().getStats().getDrownAnnounceMessage().replaceAll(
                                    "~",
                                    ((Player) event.getEntity()).getDisplayName() + ChatColor.BLUE
                            )
                    );
                    plugin.setLastDrowningTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
            // POISON ANNOUNCE
            if (plugin.config().getStats().isPoisonAnnounce())
            if (event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastPoisonTime(uuid) > 120) {

                    event.getEntity().getServer().broadcastMessage(
                            ChatColor.GREEN +
                            plugin.config().getStats().getPoisonAnnounceMessage().replaceAll(
                                    "~",
                                    ((Player) event.getEntity()).getDisplayName() + ChatColor.GREEN
                            )
                    );
                    plugin.setLastPoisonTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
            // WITHER ANNOUNCE
            if (plugin.config().getStats().isWitherAnnounce())
                if (event.getCause().equals(EntityDamageEvent.DamageCause.WITHER)) {
                    if ((System.currentTimeMillis() / 1000) - plugin.getLastWitherTime(uuid) > 120) {

                        event.getEntity().getServer().broadcastMessage(
                                ChatColor.DARK_GRAY +
                                plugin.config().getStats().getWitherAnnounceMessage().replaceAll(
                                        "~",
                                        ((Player) event.getEntity()).getDisplayName() + ChatColor.DARK_GRAY
                                )
                        );
                        plugin.setLastWitherTime(uuid, (int) (System.currentTimeMillis() / 1000));
                    }
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            final UUID uuid = event.getEntity().getUniqueId();
            final String worldName = event.getEntity().getWorld().getName();
            final int damageTaken = (int) Math.round(event.getFinalDamage());
            final Entity entity = event.getDamager();

            plugin.getThreadManager().schedule(
                QDamageTaken.class, uuid, worldName,
                (d, query, id, worldId) -> {
                    // For special entities which are clumped together
                    // currently only skeletons and wither skeletons fall under this category
                    EntityCode code = EntityCode.fromEntity(entity);

                    String entityValue;
                    if (entity instanceof Player) {
                        entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(uuid));
                    } else {
                        if (entity instanceof EnderPearl) {
                            entityValue = "Ender Pearl";
                        } else {
                            entityValue = code.getName(entity.getName());
                        }
                    }

                    return entityValue;
                }, (t, clause, id, worldId, entityValue) ->
                    clause.columns(t.id, t.worldId, t.entity, t.amount)
                        .values(id, worldId, entityValue, damageTaken).execute(),
                (t, clause, id, worldId, entityValue) ->
                    clause.where(t.id.eq(id), t.worldId.eq(worldId), t.entity.eq(entityValue))
                        .set(t.amount, t.amount.add(damageTaken)).execute()
            );
        }
    }
}
