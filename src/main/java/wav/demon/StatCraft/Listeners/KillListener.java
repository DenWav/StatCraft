package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import wav.demon.StatCraft.Magic.EntityCode;
import wav.demon.StatCraft.Querydsl.Kills;
import wav.demon.StatCraft.Querydsl.QKills;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class KillListener implements Listener {

    private StatCraft plugin;

    public KillListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            final UUID uuid = event.getEntity().getKiller().getUniqueId();
            final String entity = event.getEntity().getName();
            final EntityCode code = EntityCode.fromEntity(event.getEntity());

            plugin.getWorkerThread().schedule(Kills.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QKills k = QKills.kills;

                    if (code == null) {
                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(k);

                            if (clause == null)
                                return;

                            clause.columns(k.id, k.entity, k.amount)
                                .values(id, entity, 1).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(k);

                            if (clause == null)
                                return;

                            clause.where(
                                k.id.eq(id),
                                k.entity.eq(entity)
                            ).set(k.amount, k.amount.add(1)).execute();
                        }
                    } else {
                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(k);

                            if (clause == null)
                                return;

                            clause.columns(k.id, k.entity, k.type, k.amount)
                                .values(id, entity, code.getCode(), 1).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(k);

                            if (clause == null)
                                return;

                            clause.where(
                                k.id.eq(id),
                                k.entity.eq(entity),
                                k.type.eq(code.getCode())
                            ).set(k.amount, k.amount.add(1)).execute();
                        }
                    }
                }
            });
        }
    }
}
