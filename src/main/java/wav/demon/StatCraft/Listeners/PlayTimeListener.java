package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wav.demon.StatCraft.Querydsl.Joins;
import wav.demon.StatCraft.Querydsl.LastJoinTime;
import wav.demon.StatCraft.Querydsl.LastLeaveTime;
import wav.demon.StatCraft.Querydsl.PlayTime;
import wav.demon.StatCraft.Querydsl.Players;
import wav.demon.StatCraft.Querydsl.QJoins;
import wav.demon.StatCraft.Querydsl.QLastJoinTime;
import wav.demon.StatCraft.Querydsl.QLastLeaveTime;
import wav.demon.StatCraft.Querydsl.QPlayTime;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.UUID;

public class PlayTimeListener implements Listener {

    private StatCraft plugin;

    public PlayTimeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);
        final byte[] array = Util.UUIDToByte(event.getPlayer().getUniqueId());

        // This MUST be done before the other two jobs, so do it in the main thread
        QPlayers p = QPlayers.players;
        SQLQuery query = plugin.getDatabaseManager().getNewQuery();
        if (query == null)
            return;
        Players result = query.from(p).where(p.uuid.eq(array)).uniqueResult(p);

        if (result == null) {
            SQLUpdateClause update = plugin.getDatabaseManager().getUpdateClause(p);
            // Blank out any conflicting names
            update.where(p.name.eq(name))
                .set(p.name, "")
                .execute();
            SQLInsertClause insert = plugin.getDatabaseManager().getInsertClause(p);
            // Insert new player listing
            insert.columns(p.uuid, p.name)
                .values(array, name)
                .execute();
        } else if (!result.getName().equals(name)) {
            SQLUpdateClause update = plugin.getDatabaseManager().getUpdateClause(p);
            // Blank out any conflicting names
            update.where(p.name.eq(name))
                .set(p.name, "")
                .execute();
            // Change name of UUID player
            update.where(p.uuid.eq(array))
                .set(p.name, name)
                .execute();
        }

        plugin.players.put(name, uuid);

        final int id = plugin.getDatabaseManager().getPlayerId(uuid);

        plugin.getWorkerThread().schedule(Joins.class, new Runnable() {
            @Override
            public void run() {
                QJoins j = QJoins.joins;
                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                if (query.from(j).where(j.id.eq(id)).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(j);
                    clause.where(j.id.eq(id)).set(j.amount, j.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(j);
                    clause.columns(j.id, j.amount).values(id, 1).execute();
                }
            }
        });

        plugin.getWorkerThread().schedule(LastJoinTime.class, new Runnable() {
            @Override
            public void run() {
                QLastJoinTime l = QLastJoinTime.lastJoinTime;
                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                if (query.from(l).where(l.id.eq(id)).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(l);
                    clause.where(l.id.eq(id)).set(l.time, currentTime).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(l);
                    clause.columns(l.id, l.time).values(id, currentTime).execute();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getWorkerThread().schedule(LastLeaveTime.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;

                QLastLeaveTime l = QLastLeaveTime.lastLeaveTime;
                if (query.from(l).where(l.id.eq(id)).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(l);
                    clause.where(l.id.eq(id)).set(l.time, currentTime).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(l);
                    clause.columns(l.id, l.time).values(id, currentTime).execute();
                }
            }
        });

        plugin.getWorkerThread().schedule(PlayTime.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;

                QLastJoinTime j = QLastJoinTime.lastJoinTime;
                Integer lastJoinTime = query.from(j).where(j.id.eq(id)).uniqueResult(j.time);
                lastJoinTime = lastJoinTime == null ? 0 : lastJoinTime;

                if (lastJoinTime != 0) {
                    int playTime = currentTime - lastJoinTime;

                    QPlayTime p = QPlayTime.playTime;
                    if (query.from(p).where(p.id.eq(id)).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(p);
                        clause.where(p.id.eq(id)).set(p.amount, p.amount.add(playTime)).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(p);
                        clause.columns(p.id, p.amount).values(id, playTime).execute();
                    }
                }
            }
        });
    }
}
