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
import com.demonwav.statcraft.querydsl.QItemDrops
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class ItemDropListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemDrop(event: PlayerDropItemEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val itemid = event.itemDrop.itemStack.typeId.toShort()
        val damage = Util.damageValue(itemid, event.itemDrop.itemStack.data.data.toShort())
        val amount = event.itemDrop.itemStack.amount

        plugin.threadManager.schedule<QItemDrops>(
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
