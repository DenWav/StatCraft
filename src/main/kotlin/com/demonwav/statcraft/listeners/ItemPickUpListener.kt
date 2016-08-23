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
import com.demonwav.statcraft.Util
import com.demonwav.statcraft.querydsl.QItemPickups
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

class ItemPickUpListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemPickup(event: PlayerPickupItemEvent) {
        val itemid = event.item.itemStack.typeId.toShort()
        val damage = Util.damageValue(itemid, event.item.itemStack.data.data.toShort())
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val amount = event.item.itemStack.amount

        plugin.threadManager.schedule<QItemPickups>(
            uuid, worldName,
            { i, clause, id, worldId ->
                clause.columns(i.id, i.worldId, i.item, i.damage, i.amount).values(id, worldId, itemid, damage, amount).execute()
            }, { i, clause, id, worldId ->
                clause.where(i.id.eq(id), i.worldId.eq(worldId), i.item.eq(itemid), i.damage.eq(damage))
                    .set(i.amount, i.amount.add(amount)).execute()
            }
        )
    }
}
