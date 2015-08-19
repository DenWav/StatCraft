package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
            final LivingEntity entity = event.getEntity();
            final EntityCode code = EntityCode.fromEntity(event.getEntity());



            plugin.getThreadManager().schedule(Kills.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    String entityValue;
                    if (entity instanceof Player) {
                        entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                    } else {
                        if (entity instanceof EnderPearl) {
                            entityValue = "Ender Pearl";
                        } else {
                            entityValue = entity.getName();
                        }
                    }

                    QKills k = QKills.kills;

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(k);

                        if (clause == null)
                            return;

                        clause.columns(k.id, k.entity, k.type, k.amount)
                            .values(id, entityValue, code.getCode(), 1).execute();
                    } catch (QueryException e) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(k);

                        if (clause == null)
                            return;

                        clause.where(
                            k.id.eq(id),
                            k.entity.eq(entityValue),
                            k.type.eq(code.getCode())
                        ).set(k.amount, k.amount.add(1)).execute();
                    }
                }
            });
        }
    }
}
