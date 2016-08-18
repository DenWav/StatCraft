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
import com.demonwav.statcraft.querydsl.QBucketFill
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketFillEvent

class BucketFillListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBucketFill(event: PlayerBucketFillEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val code: BucketCode
        when (event.itemStack.type) {
            Material.MILK_BUCKET -> code = BucketCode.MILK
            Material.LAVA_BUCKET -> code = BucketCode.LAVA
            else -> code = BucketCode.WATER // default to water
        }

        plugin.threadManager.schedule<QBucketFill>(
            uuid, worldName,
            { f, clause, id, worldId ->
                clause.columns(f.id, f.worldId, f.type, f.amount)
                    .values(id, worldId, code.code, 1).execute()
            }, { f, clause, id, worldId ->
                clause.where(f.id.eq(id), f.worldId.eq(worldId), f.type.eq(code.code))
                    .set(f.amount, f.amount.add(1)).execute()
            }
        )
    }
}
