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
import com.demonwav.statcraft.magic.ProjectilesCode;
import com.demonwav.statcraft.querydsl.QProjectiles;

import com.mysema.query.types.expr.CaseBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.UUID;

public class SnowballListener implements Listener {

    private final StatCraft plugin;

    public SnowballListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSnowball(final ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) event.getEntity().getShooter();
            final Location playerLocation = player.getLocation();
            final Location snowballLocation = event.getEntity().getLocation();

            final double distance = playerLocation.distance(snowballLocation);
            final int finalDistance = (int) Math.round(distance * 100.0);

            final UUID uuid = player.getUniqueId();
            final String worldName = player.getWorld().getName();

            plugin.getThreadManager().schedule(
                QProjectiles.class, uuid, worldName,
                (p, clause, id, worldId) ->
                    clause.columns(p.id, p.worldId, p.type, p.amount, p.totalDistance, p.maxThrow)
                        .values(id, worldId, ProjectilesCode.SNOWBALL.getCode(), 1, finalDistance, finalDistance).execute(),
                (p, clause, id, worldId) ->
                    clause.where(p.id.eq(id), p.worldId.eq(worldId), p.type.eq(ProjectilesCode.SNOWBALL.getCode()))
                        .set(p.amount, p.amount.add(1))
                        .set(p.totalDistance, p.totalDistance.add(finalDistance))
                        .set(p.maxThrow,
                            new CaseBuilder()
                                .when(p.maxThrow.lt(finalDistance)).then(finalDistance)
                                .otherwise(p.maxThrow))
                        .execute()
            );
        }
    }
}
