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
import com.demonwav.statcraft.querydsl.QOnFire;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class OnFireListener implements Listener {

    private StatCraft plugin;

    public OnFireListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFire(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

                final UUID uuid = event.getEntity().getUniqueId();
                final UUID worldUuid = event.getEntity().getWorld().getUID();

                plugin.getThreadManager().schedule(
                    QOnFire.class, uuid, worldUuid,
                    (o, clause, id, worldId) ->
                        clause.columns(o.id, o.worldId, o.time).values(id, worldId, 1).execute(),
                    (o, clause, id, worldId) ->
                        clause.where(o.id.eq(id), o.worldId.eq(worldId)).set(o.time, o.time.add(1)).execute()
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        if (plugin.config().getStats().isOnFireAnnounce())
        if (event.getEntity() instanceof Player) {
            UUID uuid = event.getEntity().getUniqueId();
            if ((System.currentTimeMillis() / 1000) - plugin.getLastFireTime(uuid) > 60) {
                boolean giveWarning = true;
                for (PotionEffect pe : ((Player) event.getEntity()).getActivePotionEffects()) {
                    if (pe.getType().getName().equalsIgnoreCase(PotionEffectType.FIRE_RESISTANCE.getName()))
                        giveWarning = false;
                }
                if (giveWarning) {
                    event.getEntity().getServer().broadcastMessage(
                        ChatColor.RED +
                        plugin.config().getStats().getOnFireAnnounceMessage().replaceAll(
                            "~",
                            ((Player) event.getEntity()).getDisplayName() + ChatColor.RED
                        )
                    );
                    plugin.setLastFireTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
        }
    }
}
