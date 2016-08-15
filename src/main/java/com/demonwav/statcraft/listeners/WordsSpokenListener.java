/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.querydsl.QMessagesSpoken;
import com.demonwav.statcraft.querydsl.QSeen;
import com.demonwav.statcraft.querydsl.QWordFrequency;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class WordsSpokenListener implements Listener {

    private final StatCraft plugin;

    public WordsSpokenListener(final StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpokenMessage(final AsyncPlayerChatEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String worldName = event.getPlayer().getWorld().getName();
        final String[] message = event.getMessage().trim().split("\\s+|[\\-_]+");
        final int currentTime = (int) (System.currentTimeMillis() / 1000L);

        final List<String> words = new LinkedList<>();

        for (String word : message) {
            String modified = word.replaceAll("[^\\w]+", "").toLowerCase();
            if (modified.length() >= 2)
                words.add(modified);
        }

        plugin.getThreadManager().schedule(
            QSeen.class, uuid, worldName,
            (s, clause, id, worldId) ->
                clause.columns(s.id, s.lastSpokeTime).values(id, currentTime).execute(),
            (s, clause, id, worldId) ->
                clause.where(s.id.eq(id)).set(s.lastSpokeTime, currentTime).execute()
        );

        plugin.getThreadManager().schedule(
            QMessagesSpoken.class, uuid, worldName,
            (m, clause, id, worldId) ->
                clause.columns(m.id, m.worldId, m.amount, m.wordsSpoken).values(id, worldId, 1, words.size()).execute(),
            (m, clause, id, worldID) ->
                clause.where(m.id.eq(id), m.worldId.eq(worldID))
                    .set(m.amount, m.amount.add(1))
                    .set(m.wordsSpoken, m.wordsSpoken.add(words.size()))
                    .execute()
        );


        if (plugin.config().getStats().isSpecificWordsSpoken()) {
            for (String word : words) {
                plugin.getThreadManager().schedule(
                    QWordFrequency.class, uuid, worldName,
                    (w, clause, id, worldId) ->
                        clause.columns(w.id, w.worldId, w.word, w.amount).values(id, worldId, word, 1).execute(),
                    (w, clause, id, worldId) ->
                        clause.where(w.id.eq(id), w.worldId.eq(worldId), w.word.eq(word)).set(w.amount, w.amount.add(1)).execute()
                );
            }
        }

    }
}
