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
import com.demonwav.statcraft.querydsl.QAnimalsBred;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class AnimalsBredListener implements Listener {

    // TODO: figure out how to implement this

    private StatCraft plugin;
    private HashMap<UUID, Player> breedMap = new HashMap<>();

    public AnimalsBredListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnimalSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
            if (event.getEntity() instanceof Ageable) {
                Ageable entity = (Ageable) event.getEntity();

                switch (entity.getType()) {
                    case HORSE:
                    case PIG:
                    case RABBIT:
                    case SHEEP:
                    case COW:
                    case MUSHROOM_COW:
                    case CHICKEN:
                    case OCELOT:
                    case WOLF:
//                        Ageable[] parents = entity.getParents();
                        Ageable[] parents = new Ageable[0];
                        if (parents.length != 0) {
                            Player firstPlayer = null;
                            for (int i = 0; i < parents.length; i++) {
                                Player player = breedMap.get(parents[i].getUniqueId());

                                if (player != null) {
                                    // Only register a player once if he fed both animals
                                    if (i == 1 && player.equals(firstPlayer)) {
                                        return;
                                    } else {
                                        firstPlayer = player;
                                    }
                                    breedMap.remove(parents[i].getUniqueId());

                                    final UUID uuid = player.getUniqueId();
                                    final String type = entity.getType().name();

                                    plugin.getThreadManager().schedule(
                                        QAnimalsBred.class, uuid,
                                        (a, clause, id) ->
                                            clause.columns(a.id, a.animal, a.amount)
                                                .values(id, type, 1).execute(),
                                        (a, clause, id) ->
                                            clause.where(a.id.eq(id), a.animal.eq(type))
                                                .set(a.amount, a.amount.add(1)).execute()
                                    );
                                }
                            }
                        }
                        break;
                }

            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFeed(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Material inHand = player.getItemInHand().getType();

        switch (inHand) {
            case GOLDEN_APPLE:
            case GOLDEN_CARROT:
            case CARROT_ITEM:
            case YELLOW_FLOWER:
                // Pig
                // Horse
                // Rabbit
                if (entity instanceof Horse && (inHand == Material.GOLDEN_APPLE || inHand == Material.GOLDEN_CARROT)) {
                    checkMob(player, (Ageable) entity);
                } else if (entity instanceof Pig && inHand == Material.CARROT_ITEM) {
                    checkMob(player, (Ageable) entity);
                } else if (entity instanceof Rabbit &&
                    (inHand == Material.CARROT_ITEM || inHand == Material.YELLOW_FLOWER || inHand == Material.GOLDEN_CARROT)) {
                    checkMob(player, (Ageable) entity);
                }
                break;
            case WHEAT:
                // Sheep
                // Cow
                // Mooshroom
                if (entity instanceof Sheep || entity instanceof Cow) {
                    checkMob(player, (Ageable) entity);
                }
                break;
            case SEEDS:
                // Chicken
                if (entity instanceof Chicken) {
                    checkMob(player, (Ageable) entity);
                }
                break;
            case RAW_FISH:
                // Cat
                if (entity instanceof Ocelot) {
                    checkMob(player, (Ageable) entity);
                }
                break;
            case RAW_BEEF:
            case RAW_CHICKEN:
            case PORK:
            case MUTTON:
            case RABBIT:
            case COOKED_BEEF:
            case COOKED_CHICKEN:
            case COOKED_MUTTON:
            case COOKED_RABBIT:
            case GRILLED_PORK:
            case ROTTEN_FLESH:
                // Wolf
                if (entity instanceof Wolf) {
                    checkMob(player, (Ageable) entity);
                }
        }
    }

    private void checkMob(Player player, Ageable entity) {
        final UUID uuid = entity.getUniqueId();
        if (entity.canBreed()) {
            Player breedPlayer = breedMap.get(uuid);
            if (breedPlayer == null) {
                // Register this mob
                breedMap.put(entity.getUniqueId(), player);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> breedMap.remove(uuid), 600);
            }
        }
    }
}
