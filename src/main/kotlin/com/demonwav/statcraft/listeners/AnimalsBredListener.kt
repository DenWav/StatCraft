/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.querydsl.QAnimalsBred
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.HashMap
import java.util.UUID

class AnimalsBredListener(private val plugin: StatCraft) : Listener {
    private val breedMap = HashMap<UUID, Player>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAnimalSpawn(event: Any) {
        val entity = Ageable::class.java.newInstance()//event.getEntity();

        when (entity.type) {
            EntityType.HORSE,
            EntityType.PIG,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.MUSHROOM_COW,
            EntityType.CHICKEN,
            EntityType.OCELOT,
            EntityType.WOLF -> {
                val parents = arrayOfNulls<Ageable>(0) // entity.getParents();
                if (parents.size != 0) {
                    var firstPlayer: Player? = null
                    for (i in parents.indices) {
                        val player = breedMap[parents[i]?.uniqueId]

                        if (player != null) {
                            // Only register a player once if (s)he fed both animals
                            if (i == 1 && player == firstPlayer) {
                                return
                            } else {
                                firstPlayer = player
                            }
                            breedMap.remove(parents[i]?.uniqueId)

                            val uuid = player.uniqueId
                            val worldName = entity.world.name
                            val type = entity.type.name

                            plugin.threadManager.schedule<QAnimalsBred>(
                                uuid, worldName,
                                { a, clause, id, worldId ->
                                    clause.columns(a.id, a.worldId, a.animal, a.amount)
                                        .values(id, worldId, type, 1).execute()
                                }, { a, clause, id, worldId ->
                                    clause.where(a.id.eq(id), a.worldId.eq(worldId), a.animal.eq(type))
                                        .set(a.amount, a.amount.add(1)).execute()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
