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
import com.demonwav.statcraft.querydsl.QMessagesSpoken
import com.demonwav.statcraft.querydsl.QSeen
import com.demonwav.statcraft.querydsl.QWordFrequency
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.LinkedList

class WordsSpokenListener(private val plugin: StatCraft) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSpokenMessage(event: AsyncPlayerChatEvent) {
        val uuid = event.player.uniqueId
        val worldName = event.player.world.name
        val message = event.message.trim { it <= ' ' }.split("\\s+|[\\-_]+".toRegex()).dropLastWhile { it.isEmpty() }
        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

        val words = LinkedList<String>()

        for (word in message) {
            val modified = word.replace("[^\\w]+".toRegex(), "").toLowerCase()
            if (modified.length >= 2) {
                words.add(modified)
            }
        }

        plugin.threadManager.schedule<QSeen>(
            uuid, worldName,
            { s, clause, id, worldId ->
                clause.columns(s.id, s.lastSpokeTime).values(id, currentTime).execute()
            }, { s, clause, id, worldId ->
                clause.where(s.id.eq(id)).set(s.lastSpokeTime, currentTime).execute()
            }
        )

        plugin.threadManager.schedule<QMessagesSpoken>(
            uuid, worldName,
            { m, clause, id, worldId ->
                clause.columns(m.id, m.worldId, m.amount, m.wordsSpoken).values(id, worldId, 1, words.size).execute()
            }, { m, clause, id, worldID ->
                clause.where(m.id.eq(id), m.worldId.eq(worldID))
                    .set(m.amount, m.amount.add(1))
                    .set(m.wordsSpoken, m.wordsSpoken.add(words.size))
                    .execute()
            }
        )

        if (plugin.config.stats.specificWordsSpoken) {
            for (word in words) {
                plugin.threadManager.schedule<QWordFrequency>(
                    uuid, worldName,
                    { w, clause, id, worldId ->
                        clause.columns(w.id, w.worldId, w.word, w.amount).values(id, worldId, word, 1).execute()
                    }, { w, clause, id, worldId ->
                        clause.where(w.id.eq(id), w.worldId.eq(worldId), w.word.eq(word)).set(w.amount, w.amount.add(1)).execute()
                    }
                )
            }
        }
    }
}
