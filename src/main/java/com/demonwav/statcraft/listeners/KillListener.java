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
import com.demonwav.statcraft.querydsl.QKills;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class KillListener implements Listener {

    private final StatCraft plugin;

    public KillListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            final UUID uuid = event.getEntity().getKiller().getUniqueId();
            final UUID worldUuid = event.getEntity().getWorld().getUID();
            final LivingEntity entity = event.getEntity();
            final EntityCode code = EntityCode.fromEntity(event.getEntity());

            plugin.getThreadManager().schedule(
                QKills.class, uuid, worldUuid,
                (k, query, id, worldId) -> {
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

                    return entityValue;
                }, (k, clause, id, worldId, entityValue) ->
                    clause.columns(k.id, k.worldId, k.entity, k.amount)
                        .values(id, worldId, entityValue, 1).execute(),
                (k, clause, id, worldId, entityValue) ->
                    clause.where(
                        k.id.eq(id),
                        k.worldId.eq(worldId),
                        k.entity.eq(entityValue)
                    ).set(k.amount, k.amount.add(1)).execute()
            );
        }
    }
}
