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
import com.demonwav.statcraft.querydsl.QOnFire;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class OnFireListener implements Listener {

    private final StatCraft plugin;
    private final HashMap<UUID, String> lastSource = new HashMap<>();

    public OnFireListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFire(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

                final UUID uuid = event.getEntity().getUniqueId();
                final String worldName = event.getEntity().getWorld().getName();

                final String source = lastSource.get(uuid);

                plugin.getThreadManager().schedule(
                    QOnFire.class, uuid, worldName,
                    (o, clause, id, worldId) ->
                        clause.columns(o.id, o.worldId, o.source, o.time).values(id, worldId, source, 1).execute(),
                    (o, clause, id, worldId) ->
                        clause.where(o.id.eq(id), o.worldId.eq(worldId), o.source.eq(source)).set(o.time, o.time.add(1)).execute()
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombust(final EntityCombustEvent event) {
        if (!plugin.config().getStats().isOnFireAnnounce()) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            UUID uuid = event.getEntity().getUniqueId();
            if ((System.currentTimeMillis() / 1000) - plugin.getLastFireTime(uuid) > 60) {
                boolean giveWarning = ((Player) event.getEntity()).getActivePotionEffects().stream().anyMatch(
                    effect -> effect.getType() == PotionEffectType.FIRE_RESISTANCE
                );
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombustByBlock(final EntityCombustByBlockEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            lastSource.put(player.getUniqueId(), "BLOCK");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombustByEntity(final EntityCombustByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (event.getCombuster() instanceof Player) {
                int id = plugin.getDatabaseManager().getPlayerId(event.getCombuster().getUniqueId());
                lastSource.put(player.getUniqueId(), String.valueOf(id));
            } else {
                lastSource.put(player.getUniqueId(), event.getCombuster().getName());
            }
        }
    }
}
