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
import com.demonwav.statcraft.magic.EntityCode
import com.demonwav.statcraft.querydsl.QDamageDealt
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DamageDealtListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamageDealt(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val damagee = event.entity
        if (damager is Player) {
            val uuid = damager.getUniqueId()
            val worldName = damager.getWorld().name
            val damageDealt = Math.round(event.finalDamage).toInt()

            if (damagee is LivingEntity) {
                val entity = event.entity as LivingEntity

                plugin.threadManager.schedule<QDamageDealt, String>(
                    uuid, worldName,
                    { d, query, id, worldId ->
                        // For special entities which are clumped together
                        // currently only skeletons and wither skeletons fall under this category
                        val code = EntityCode.fromEntity(entity)
                        if (entity is Player) {
                            plugin.databaseManager.getPlayerId(entity.getUniqueId()).toString()
                        } else {
                            code.getName(entity.name)
                        }
                    }, { d, clause, id, worldId, entityValue ->
                        clause.columns(d.id, d.worldId, d.entity, d.amount)
                            .values(id, worldId, entityValue, damageDealt).execute()
                    }, { d, clause, id, worldId, entityValue ->
                        clause.where(d.id.eq(id), d.worldId.eq(worldId), d.entity.eq(entityValue))
                            .set(d.amount, d.amount.add(damageDealt)).execute()
                    }
                )
            }
        }
    }
}
