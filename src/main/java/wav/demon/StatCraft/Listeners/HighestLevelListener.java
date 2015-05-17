package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import wav.demon.StatCraft.Querydsl.HighestLevel;
import wav.demon.StatCraft.Querydsl.QHighestLevel;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class HighestLevelListener implements Listener {

    private StatCraft plugin;

    public HighestLevelListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevel(PlayerLevelChangeEvent event) {
        final int newLevel = event.getNewLevel();
        final UUID uuid = event.getPlayer().getUniqueId();

        plugin.getWorkerThread().schedule(HighestLevel.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QHighestLevel h = QHighestLevel.highestLevel;

                Integer currentLevel = query.from(h).where(h.id.eq(id)).uniqueResult(h.level);
                if (currentLevel == null) {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(h);
                    clause.columns(h.id, h.level)
                        .values(id, newLevel).execute();
                } else if (currentLevel > newLevel) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(h);
                    clause.where(h.id.eq(id)).set(h.level, newLevel).execute();
                }
            }
        });
    }
}
