package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;
import wav.demon.StatCraft.Querydsl.EggsThrown;
import wav.demon.StatCraft.Querydsl.QEggsThrown;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class EggListener implements Listener {

    private StatCraft plugin;

    public EggListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEggHit(PlayerEggThrowEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final boolean hatched = event.isHatching();

        plugin.getWorkerThread().schedule(EggsThrown.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QEggsThrown e = QEggsThrown.eggsThrown;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);

                    if (clause == null)
                        return;

                    clause.columns(e.id, e.hatched, e.amount)
                        .values(id, hatched, 1).execute();
                } catch (QueryException ex) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);

                    if (clause == null)
                        return;

                    clause.where(e.id.eq(id), e.hatched.eq(hatched)).set(e.amount, e.amount.add(1)).execute();
                }
            }
        });
    }

}
