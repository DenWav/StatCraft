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
import com.demonwav.statcraft.magic.BucketCode
import com.demonwav.statcraft.querydsl.QBucketEmpty
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

class BucketEmptyListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val code: BucketCode
        if (event.bucket == Material.LAVA_BUCKET) {
            code = BucketCode.LAVA
        } else {
            // default to water
            code = BucketCode.WATER
        }

        plugin.threadManager.schedule<QBucketEmpty>(
            uuid, worldName,
            { e, clause, id, worldId ->
                clause.columns(e.id, e.worldId, e.type, e.amount)
                    .values(id, worldId, code.code, 1).execute()
            }, { e, clause, id, worldId ->
                clause.where(e.id.eq(id), e.worldId.eq(worldId), e.type.eq(code.code))
                    .set(e.amount, e.amount.add(1)).execute()
            }
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.MILK_BUCKET) {
            val uuid = event.player.uniqueId
            val worldName = event.player.world.name
            val code = BucketCode.MILK

            plugin.threadManager.schedule<QBucketEmpty>(
                uuid, worldName,
                { e, clause, id, worldId ->
                    clause.columns(e.id, e.worldId, e.type, e.amount)
                        .values(id, worldId, code.code, 1).execute()
                }, { e, clause, id, worldId ->
                    clause.where(e.id.eq(id), e.worldId.eq(worldId), e.type.eq(code.code))
                        .set(e.amount, e.amount.add(1)).execute()
                }
            )
        }
    }
}
