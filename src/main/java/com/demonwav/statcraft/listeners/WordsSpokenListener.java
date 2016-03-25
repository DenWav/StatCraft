/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.listeners;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;
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

    private StatCraft plugin;

    public WordsSpokenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpokenMessage(AsyncPlayerChatEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String[] message = event.getMessage().trim().split("\\s+|[\\-_]+");
        final int currentTime = (int) (System.currentTimeMillis() / 1000L);

        final List<String> words = new LinkedList<>();

        for (String word : message) {
            String modified = word.replaceAll("[^\\w]+", "").toLowerCase();
            if (modified.length() >= 2)
                words.add(modified);
        }

        plugin.getThreadManager().schedule(
            QSeen.class, uuid,
            (s, clause, id) ->
                clause.columns(s.id, s.lastSpokeTime).values(id, currentTime).execute(),
            (s, clause, id) ->
                clause.where(s.id.eq(id)).set(s.lastSpokeTime, currentTime).execute()
        );

        plugin.getThreadManager().schedule(
            QMessagesSpoken.class, uuid,
            (m, clause, id) ->
                clause.columns(m.id, m.amount, m.wordsSpoken).values(id, 1, words.size()).execute(),
            (m, clause, id) ->
                clause.where(m.id.eq(id)).set(m.amount, m.amount.add(1))
                    .set(m.wordsSpoken, m.wordsSpoken.add(words.size())).execute()
        );

        plugin.getThreadManager().scheduleRaw(
            QWordFrequency.class, (connection) -> {
                if (plugin.config().getStats().isSpecificWordsSpoken()) {
                    for (String word : words) {
                        Util.runQuery(
                            QWordFrequency.class, uuid,
                            (w, clause, id) ->
                                clause.columns(w.id, w.word, w.amount).values(id, word, 1).execute(),
                            (w, clause, id) ->
                                clause.where(w.id.eq(id), w.word.eq(word)).set(w.amount, w.amount.add(1)).execute(),
                            connection, plugin
                        );
                    }
                } else {
                    Util.runQuery(
                        QWordFrequency.class, uuid,
                        (w, clause, id) ->
                            clause.columns(w.id, w.word, w.amount).values(id, "§", 1).execute(),
                        (w, clause, id) ->
                            clause.where(w.id.eq(id), w.word.eq("§")).set(w.amount, w.amount.add(1)).execute(),
                        connection, plugin
                    );
                }
            }
        );
    }
}
