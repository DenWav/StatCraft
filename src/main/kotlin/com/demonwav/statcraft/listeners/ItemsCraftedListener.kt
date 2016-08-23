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
import com.demonwav.statcraft.querydsl.QItemsCrafted
import com.google.common.base.Objects
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class ItemsCraftedListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemCraft(event: CraftItemEvent) {
        val player = event.whoClicked
        val toCraft = event.currentItem
        val toStore = event.cursor

        // Make sure we are actually crafting anything
        if (player != null && hasItems(toCraft)) {
            if (event.isShiftClick) {
                // Hack ahoy
                schedulePostDetection(player, toCraft)
            } else {
                // The items are stored in the cursor. Make sure there's enough space.
                if (isStackSumLegal(toCraft, toStore)) {
                    val newItemsCount = toCraft.amount

                    val item = toCraft.type.id.toShort()
                    val damage = toCraft.data.data.toShort()
                    val uuid = player.uniqueId
                    val worldName = player.world.name

                    updateData(item, damage, uuid, worldName, newItemsCount)
                }
            }
        }
    }

    private fun updateData(itemid: Short, initDamage: Short, uuid: UUID, worldName: String, amount: Int) {
        val damage = Util.damageValue(itemid, initDamage)
        plugin.threadManager.schedule<QItemsCrafted>(
            uuid, worldName,
            { i, clause, id, worldId ->
                clause.columns(i.id, i.worldId, i.item, i.damage, i.amount).values(id, worldId, itemid, damage, amount).execute()
            }, { i, clause, id, worldId ->
                clause.where(i.id.eq(id), i.worldId.eq(worldId), i.item.eq(itemid), i.damage.eq(damage))
                    .set(i.amount, i.amount.add(amount)).execute()
            }
        )
    }

    /** From here down is Comphenix's code  */
    // HACK! The API doesn't allow us to easily determine the resulting number of
    // crafted items, so we're forced to compare the inventory before and after.
    private fun schedulePostDetection(player: HumanEntity, compareItem: ItemStack) {
        val preInv = player.inventory.contents
        val ticks = 1L

        // Clone the array. The content may (was for me) be mutable.
        for (i in preInv.indices) {
            preInv[i] = if (preInv[i] != null) preInv[i].clone() else null
        }

        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val postInv = player.inventory.contents
            var newItemsCount = 0

            for (i in preInv.indices) {
                val pre = preInv[i]
                val post = postInv[i]

                // We're only interested in filled slots that are different
                if (hasSameItem(compareItem, post) && (hasSameItem(compareItem, pre) || pre == null)) {
                    newItemsCount += post.amount - (if (pre != null) pre.amount else 0)
                }
            }

            if (newItemsCount > 0) {
                val item = compareItem.type.id.toShort()
                val damage = compareItem.data.data.toShort()
                val uuid = player.uniqueId
                val worldName = player.world.name

                updateData(item, damage, uuid, worldName, newItemsCount)
            }
        }, ticks)
    }

    private fun hasSameItem(a: ItemStack?, b: ItemStack?): Boolean {
        if (a == null) {
            return b == null
        } else if (b == null) {
            return false
        }

        return a.typeId == b.typeId &&
            a.durability == b.durability &&
            Objects.equal(a.data, b.data) &&
            Objects.equal(a.enchantments, b.enchantments)
    }

    private fun isStackSumLegal(a: ItemStack?, b: ItemStack?): Boolean {
        // See if we can create a new item stack with the combined elements of a and b
        return a == null || b == null || a.amount + b.amount <= a.type.maxStackSize
    }

    private fun hasItems(stack: ItemStack?): Boolean {
        return stack != null && stack.amount > 0
    }
}
