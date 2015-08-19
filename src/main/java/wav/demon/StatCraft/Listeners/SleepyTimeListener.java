package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import wav.demon.StatCraft.Querydsl.EnterBed;
import wav.demon.StatCraft.Querydsl.LeaveBed;
import wav.demon.StatCraft.Querydsl.QEnterBed;
import wav.demon.StatCraft.Querydsl.QLeaveBed;
import wav.demon.StatCraft.Querydsl.QTimeSlept;
import wav.demon.StatCraft.Querydsl.TimeSlept;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class SleepyTimeListener implements Listener {

    private StatCraft plugin;

    public SleepyTimeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(EnterBed.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QEnterBed e = QEnterBed.enterBed;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                    if (clause == null)
                        return;
                    clause.columns(e.id, e.time).values(id, currentTime).execute();
                } catch (QueryException ex) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                    if (clause == null)
                        return;
                    clause.where(e.id.eq(id)).set(e.time, currentTime).execute();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        plugin.getThreadManager().schedule(LeaveBed.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QLeaveBed l = QLeaveBed.leaveBed;

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

        plugin.getThreadManager().schedule(TimeSlept.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QEnterBed e = QEnterBed.enterBed;
                Integer enterBed = query.from(e).where(e.id.eq(id)).uniqueResult(e.time);
                enterBed = enterBed == null ? 0 : enterBed;

                if (enterBed != 0) {
                    int timeSlept = currentTime - enterBed;

                    QTimeSlept t = QTimeSlept.timeSlept;

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);

                        if (clause == null)
                            return;

                        clause.columns(t.id, t.amount).values(id, timeSlept).execute();
                    } catch (QueryException ex) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);

                        if (clause == null)
                            return;

                        clause.where(t.id.eq(id)).set(t.amount, t.amount.add(timeSlept)).execute();
                    }

                }
            }
        });
    }
}
