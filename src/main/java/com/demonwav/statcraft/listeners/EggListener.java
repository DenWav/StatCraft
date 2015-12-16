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
import com.demonwav.statcraft.magic.ProjectilesCode;
import com.demonwav.statcraft.querydsl.Projectiles;
import com.demonwav.statcraft.querydsl.QProjectiles;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.expr.CaseBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

import java.util.UUID;

public class EggListener implements Listener {

    private StatCraft plugin;

    public EggListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEggHit(PlayerEggThrowEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final Location eggLocation = event.getEgg().getLocation();
        final Location playerLocation = player.getLocation();
        final boolean hatched = event.isHatching();
        final byte numberHatched = event.getNumHatches();

        final double distance = playerLocation.distance(eggLocation);
        final int finalDistance = (int) Math.round(distance * 100.0);

        plugin.getThreadManager().schedule(Projectiles.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                ProjectilesCode code;

                if (hatched && numberHatched == 1) {
                    code = ProjectilesCode.HATCHED_EGG;
                } else if (hatched) {
                    code = ProjectilesCode.FOUR_HATCHED_EGG;
                } else {
                    code = ProjectilesCode.UNHATCHED_EGG;
                }

                QProjectiles p = QProjectiles.projectiles;
                Util.projectile(plugin, p, id, code, finalDistance);
            }
        });

    }

}
