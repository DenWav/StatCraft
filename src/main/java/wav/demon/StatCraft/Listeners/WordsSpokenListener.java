package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import wav.demon.StatCraft.Querydsl.MessagesSpoken;
import wav.demon.StatCraft.Querydsl.QMessagesSpoken;
import wav.demon.StatCraft.Querydsl.QWordsSpoken;
import wav.demon.StatCraft.Querydsl.WordsSpoken;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class WordsSpokenListener implements Listener {

    private StatCraft plugin;

    public WordsSpokenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpokenMessage(AsyncPlayerChatEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String[] message = event.getMessage().trim().split("\\s+");

        plugin.getWorkerThread().schedule(MessagesSpoken.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QMessagesSpoken m = QMessagesSpoken.messagesSpoken;

                if (query.from(m).where(m.id.eq(id)).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(m);
                    clause.where(m.id.eq(id)).set(m.amount, m.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(m);
                    clause.columns(m.id, m.amount).values(id, 1).execute();
                }


            }
        });

        plugin.getWorkerThread().schedule(WordsSpoken.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QWordsSpoken w = QWordsSpoken.wordsSpoken;

                if (plugin.config().stats.specific_words_spoken) {
                    for (String word : message) {
                        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                        if (query == null)
                            return;
                        if (query.from(w).where(w.id.eq(id).and(w.word.eq(word))).exists()) {
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(w);
                            clause.where(w.id.eq(id).and(w.word.eq(word))).set(w.amount, w.amount.add(1)).execute();
                        } else {
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(w);
                            clause.columns(w.id, w.word, w.amount).values(id, word, 1).execute();
                        }
                    }
                } else {
                    SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                    if (query == null)
                        return;
                    if (query.from(w).where(w.id.eq(id).and(w.word.eq("§"))).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(w);
                        clause.where(w.id.eq(id).and(w.word.eq("§"))).set(w.amount, w.amount.add(1)).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(w);
                        clause.columns(w.id, w.word, w.amount).values(id, "§", 1).execute();
                    }
                }
            }
        });
    }
}
