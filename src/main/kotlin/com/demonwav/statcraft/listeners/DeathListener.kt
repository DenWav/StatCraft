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
import com.demonwav.statcraft.querydsl.QDeath
import com.demonwav.statcraft.querydsl.QDeathByCause
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent

class DeathListener(private val plugin: StatCraft) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(event: PlayerDeathEvent) {
        val message = event.deathMessage
        val uuid = event.entity.uniqueId
        val worldName = event.entity.world.name
        val damageEvent = event.entity.lastDamageCause

        val cause: String
        if (damageEvent is EntityDamageByEntityEvent) {
            val killer = damageEvent.damager
            val code = EntityCode.fromEntity(killer)

            if (killer is Player) {
                cause = plugin.databaseManager.getPlayerId(killer.uniqueId).toString()
            } else {
                if (killer is EnderPearl) {
                    cause = "EnderPearl"
                } else if (killer is WitherSkull) {
                    cause = "WitherSkull"
                } else {
                    cause = code.getName(killer.name)
                }
            }
        } else {
            val damageCause = damageEvent.cause
            cause = damageCause.name
        }

        plugin.threadManager.schedule<QDeath>(
            uuid, worldName,
            { d, clause, id, worldId ->
                clause.columns(d.id, d.message, d.worldId, d.amount).values(id, message, worldId, 1).execute()
            }, { d, clause, id, worldId ->
                clause.where(d.id.eq(id), d.message.eq(message), d.worldId.eq(worldId))
                    .set(d.amount, d.amount.add(1)).execute()
            }
        )

        plugin.threadManager.schedule<QDeathByCause>(
            uuid, worldName,
            { c, clause, id, worldId ->
                clause.columns(c.id, c.cause, c.worldId, c.amount)
                    .values(id, cause, worldId, 1).execute()
            }, { c, clause, id, worldId ->
                clause.where(c.id.eq(id), c.cause.eq(cause), c.worldId.eq(worldId))
                    .set(c.amount, c.amount.add(1)).execute()
            }
        )
    }
}
