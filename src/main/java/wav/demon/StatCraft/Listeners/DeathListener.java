package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import wav.demon.StatCraft.Magic.EntityCode;
import wav.demon.StatCraft.Querydsl.Death;
import wav.demon.StatCraft.Querydsl.QDeath;
import wav.demon.StatCraft.Querydsl.QDeathByEntity;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class DeathListener implements Listener {

    private StatCraft plugin;

    public DeathListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        final String message = event.getDeathMessage();
        final UUID uuid = event.getEntity().getUniqueId();
        final String world = event.getEntity().getLocation().getWorld().getName();
        String entity = null;
        EntityCode code = null;

        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            Entity killer = damageByEntityEvent.getDamager();
            entity = killer.getName();
            code = EntityCode.fromEntity(killer);
        }

        final String finalEntity = entity;
        final EntityCode finalCode = code;
        plugin.getWorkerThread().schedule(Death.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QDeath d = QDeath.death;

                if (query.from(d).where(d.id.eq(id).and(d.message.eq(message)).and(d.world.eq(world))).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(d);
                    clause.where(
                        d.id.eq(id)
                            .and(d.message.eq(message))
                            .and(d.world.eq(world))
                    ).set(d.amount, d.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(d);
                    clause.columns(d.id, d.message, d.world, d.amount).values(id, message, world, 1).execute();
                }

                if (finalEntity != null) {
                    QDeathByEntity e = QDeathByEntity.deathByEntity;

                    if (finalCode != null) {
                        if (query.from(e).where(
                            e.id.eq(id)
                                .and(e.entity.eq(finalEntity))
                                .and(e.type.eq(finalCode.getCode()))
                                .and(e.world.eq(world))
                        ).exists()) {

                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                            clause.where(
                                e.id.eq(id)
                                    .and(e.entity.eq(finalEntity))
                                    .and(e.type.eq(finalCode.getCode()))
                                    .and(e.world.eq(world))
                            ).set(d.amount, d.amount.add(1)).execute();
                        } else {
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                            clause.columns(e.id, e.entity, e.type, e.world, e.amount)
                                .values(id, finalEntity, finalCode.getCode(), world, 1).execute();
                        }
                    } else {
                        if (query.from(e).where(
                            e.id.eq(id)
                                .and(e.entity.eq(finalEntity))
                                .and(e.world.eq(world))
                        ).exists()) {

                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);
                            clause.where(
                                e.id.eq(id)
                                    .and(e.entity.eq(finalEntity))
                                    .and(e.world.eq(world))
                            ).set(e.amount, e.amount.add(1)).execute();
                        } else {
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);
                            clause.columns(e.id, e.entity, e.world, e.amount)
                                .values(id, finalEntity, world, 1).execute();
                        }
                    }
                }
            }
        });
    }
}
