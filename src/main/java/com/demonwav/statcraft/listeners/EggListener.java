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
import com.demonwav.statcraft.magic.ProjectilesCode;
import com.demonwav.statcraft.querydsl.QProjectiles;
import com.mysema.query.types.expr.CaseBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

import java.util.UUID;

public class EggListener implements Listener {

    private final StatCraft plugin;

    public EggListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEggHit(PlayerEggThrowEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final UUID worldUuid = player.getWorld().getUID();
        final Location eggLocation = event.getEgg().getLocation();
        final Location playerLocation = player.getLocation();
        final boolean hatched = event.isHatching();
        final byte numberHatched = event.getNumHatches();

        final double distance = playerLocation.distance(eggLocation);
        final int finalDistance = (int) Math.round(distance * 100.0);

        ProjectilesCode code;

        if (hatched && numberHatched == 1) {
            code = ProjectilesCode.HATCHED_EGG;
        } else if (hatched) {
            code = ProjectilesCode.FOUR_HATCHED_EGG;
        } else {
            code = ProjectilesCode.UNHATCHED_EGG;
        }

        plugin.getThreadManager().schedule(
            QProjectiles.class, uuid, worldUuid,
            (p, clause, id, worldId) ->
                clause.columns(p.id, p.worldId, p.type, p.amount, p.totalDistance, p.maxThrow)
                    .values(id, worldId, code.getCode(), 1, finalDistance, finalDistance).execute(),
            (p, clause, id, worldId) ->
                clause.where(p.id.eq(id), p.worldId.eq(worldId), p.type.eq(code.getCode())).set(p.amount, p.amount.add(1))
                    .set(p.totalDistance, p.totalDistance.add(finalDistance))
                    .set(p.maxThrow,
                        new CaseBuilder()
                            .when(p.maxThrow.lt(finalDistance)).then(finalDistance)
                            .otherwise(p.maxThrow))
                    .execute()
        );
    }

}
