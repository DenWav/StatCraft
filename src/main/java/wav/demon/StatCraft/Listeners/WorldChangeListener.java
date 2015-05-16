package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import wav.demon.StatCraft.Querydsl.QWorldChange;
import wav.demon.StatCraft.Querydsl.WorldChange;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class WorldChangeListener implements Listener {

    private StatCraft plugin;

    public WorldChangeListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String fromWorld = event.getFrom().getName();
        final String toWorld = event.getPlayer().getWorld().getName();

        plugin.getWorkerThread().schedule(WorldChange.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QWorldChange w = QWorldChange.worldChange;

                if (query.from(w).where(
                        w.id.eq(id)
                        .and(w.toWorld.eq(toWorld))
                        .and(w.fromWorld.eq(fromWorld))
                    ).exists()) {

                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(w);
                    clause.where(
                        w.id.eq(id)
                        .and(w.toWorld.eq(toWorld))
                        .and(w.fromWorld.eq(fromWorld))
                    ).set(w.amount, w.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(w);
                    clause.columns(w.id, w.toWorld, w.fromWorld, w.amount)
                        .values(id, toWorld, fromWorld, 1).execute();
                }
            }
        });
    }
}
