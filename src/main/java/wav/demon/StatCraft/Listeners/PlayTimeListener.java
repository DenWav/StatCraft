package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
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
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class PlayTimeListener implements Listener {

    private StatCraft plugin;

    public PlayTimeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        plugin.getWorkerThread().schedule(Players.class, new Runnable() {
            @Override
            public void run() {
                // This MUST be done before the other two jobs
                plugin.setupPlayer(event.getPlayer());
                plugin.players.put(name, uuid);

                // Get id to work with
                final int id = plugin.getDatabaseManager().getPlayerId(uuid);

                plugin.getWorkerThread().schedule(Joins.class, new Runnable() {
                    @Override
                    public void run() {
                        QJoins j = QJoins.joins;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(j);

                            if (clause == null)
                                return;

                            clause.columns(j.id, j.amount).values(id, 1).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(j);

                            if (clause == null)
                                return;

                            clause.where(j.id.eq(id)).set(j.amount, j.amount.add(1)).execute();
                        }
                    }
                });

                plugin.getWorkerThread().schedule(LastJoinTime.class, new Runnable() {
                    @Override
                    public void run() {
                        QLastJoinTime l = QLastJoinTime.lastJoinTime;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(l);

                            if (clause == null)
                                return;

                            clause.columns(l.id, l.time).values(id, currentTime).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(l);

                            if (clause == null)
                                return;

                            clause.where(l.id.eq(id)).set(l.time, currentTime).execute();
                        }
                    }
                });
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

                QLastLeaveTime l = QLastLeaveTime.lastLeaveTime;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(l);

                    if (clause == null)
                        return;

                    clause.columns(l.id, l.time).values(id, currentTime).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(l);

                    if (clause == null)
                        return;

                    clause.where(l.id.eq(id)).set(l.time, currentTime).execute();
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

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(p);

                        if (clause == null)
                            return;

                        clause.columns(p.id, p.amount).values(id, playTime).execute();
                    } catch (QueryException e) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(p);

                        if (clause == null)
                            return;

                        clause.where(p.id.eq(id)).set(p.amount, p.amount.add(playTime)).execute();
                    }
                }
            }
        });
    }
}
