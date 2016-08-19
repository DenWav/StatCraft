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
import com.demonwav.statcraft.magic.FishCode
import com.demonwav.statcraft.querydsl.QFishCaught
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

class FishCaughtListener(private val plugin: StatCraft) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFishCatch(event: PlayerFishEvent) {
        if (event.caught == null) {
            return
        }

        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val caught = event.caught

        if (caught is Item) {
            val itemId = caught.itemStack.typeId.toShort()
            val damage = Util.damageValue(itemId, caught.itemStack.data.data.toShort())

            val code = when (caught.itemStack.type) {
                Material.RAW_FISH ->
                    FishCode.FISH

                Material.BOW,
                Material.ENCHANTED_BOOK,
                Material.NAME_TAG,
                Material.SADDLE,
                Material.WATER_LILY ->
                    FishCode.TREASURE

                Material.BOWL,
                Material.LEATHER,
                Material.LEATHER_BOOTS,
                Material.ROTTEN_FLESH,
                Material.STICK,
                Material.STRING,
                Material.POTION,
                Material.BONE,
                Material.INK_SACK,
                Material.TRIPWIRE_HOOK ->
                    FishCode.JUNK

                Material.FISHING_ROD ->
                    if (caught.itemStack.enchantments.size == 0) {
                        FishCode.JUNK
                    } else {
                        FishCode.TREASURE
                    }

                else ->
                    FishCode.JUNK
            }

            plugin.threadManager.schedule<QFishCaught>(
                uuid, worldName,
                { f, clause, id, worldId ->
                    clause.columns(f.id, f.worldId, f.item, f.damage, f.amount)
                        .values(id, worldId, itemId, damage, code.code, 1).execute()
                }, { f, clause, id, worldId ->
                    clause.where(f.id.eq(id), f.worldId.eq(worldId), f.item.eq(itemId), f.damage.eq(damage), f.type.eq(code.code))
                        .set(f.amount, f.amount.add(1)).execute()
                }
            )
        }
    }
}
