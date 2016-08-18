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
import com.demonwav.statcraft.querydsl.QBlockBreak
import com.demonwav.statcraft.querydsl.QBlockPlace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockListener(private val plugin: StatCraft) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val blockid = event.block.typeId.toShort()
        val damage = Util.damageValue(blockid, event.block.data.toShort())
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name

        plugin.threadManager.schedule<QBlockBreak>(uuid, worldName,
            { b, clause, id, worldId ->
                clause.columns(b.id, b.worldId, b.blockid, b.damage, b.amount)
                    .values(id, worldId, blockid, damage, 1).execute()
            }, { b, clause, id, worldId ->
                clause.where(b.id.eq(id), b.worldId.eq(worldId), b.blockid.eq(blockid), b.damage.eq(damage))
                    .set(b.amount, b.amount.add(1)).execute()
            }
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val blockid = event.block.typeId.toShort()
        val damage = Util.damageValue(blockid, event.block.data.toShort())
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name

        plugin.threadManager.schedule<QBlockPlace>(
            uuid, worldName,
            { b, clause, id, worldId ->
                clause.columns(b.id, b.worldId, b.blockid, b.damage, b.amount)
                    .values(id, worldId, blockid, damage, 1).execute()
            }, { b, clause, id, worldId ->
            clause.where(b.id.eq(id), b.worldId.eq(worldId), b.blockid.eq(blockid), b.damage.eq(damage))
                .set(b.amount, b.amount.add(1)).execute()
            }
        )
    }
}
