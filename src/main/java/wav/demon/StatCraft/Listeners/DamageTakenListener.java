package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import wav.demon.StatCraft.Magic.EntityCode;
import wav.demon.StatCraft.Querydsl.DamageTaken;
import wav.demon.StatCraft.Querydsl.QDamageTaken;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class DamageTakenListener implements Listener {

    StatCraft plugin;

    public DamageTakenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageTaken(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            final UUID uuid = event.getEntity().getUniqueId();
            final int damageTaken = (int) Math.round(event.getFinalDamage());

            plugin.getWorkerThread().schedule(DamageTaken.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QDamageTaken t = QDamageTaken.damageTaken;

                    try {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);

                        if (clause == null)
                            return;

                        clause.columns(t.id, t.entity, t.amount)
                            .values(id, event.getCause().name(), damageTaken).execute();
                    } catch (QueryException e) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);

                        if (clause == null)
                            return;

                        clause.where(
                            t.id.eq(id),
                            t.entity.eq(event.getCause().name())
                        ).set(t.amount, t.amount.add(damageTaken)).execute();
                    }
                }
            });


            // DROWN ANNOUNCE
            if (plugin.config().stats.drowning_announce)
            if (event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastDrownTime(uuid) > 120) {

                    event.getEntity().getServer().broadcastMessage(
                            ChatColor.BLUE.toString() +
                            plugin.config().stats.drown_announce_message.replaceAll(
                                    "~",
                                    ((Player) event.getEntity()).getDisplayName() + ChatColor.BLUE.toString()
                            )
                    );
                    plugin.setLastDrowningTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
            // POISON ANNOUNCE
            if (plugin.config().stats.poison_announce)
            if (event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) {
                if ((System.currentTimeMillis() / 1000) - plugin.getLastPoisonTime(uuid) > 120) {

                    event.getEntity().getServer().broadcastMessage(
                            ChatColor.GREEN.toString() +
                            plugin.config().stats.poison_announce_message.replaceAll(
                                    "~",
                                    ((Player) event.getEntity()).getDisplayName() + ChatColor.GREEN.toString()
                            )
                    );
                    plugin.setLastPoisonTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
            // WITHER ANNOUNCE
            if (plugin.config().stats.wither_announce)
                if (event.getCause().equals(EntityDamageEvent.DamageCause.WITHER)) {
                    if ((System.currentTimeMillis() / 1000) - plugin.getLastWitherTime(uuid) > 120) {

                        event.getEntity().getServer().broadcastMessage(
                                ChatColor.DARK_GRAY.toString() +
                                plugin.config().stats.wither_announce_message.replaceAll(
                                        "~",
                                        ((Player) event.getEntity()).getDisplayName() + ChatColor.DARK_GRAY.toString()
                                )
                        );
                        plugin.setLastWitherTime(uuid, (int) (System.currentTimeMillis() / 1000));
                    }
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            final UUID uuid = event.getEntity().getUniqueId();
            final int damageTaken = (int) Math.round(event.getFinalDamage());
            final Entity entity = event.getDamager();

            plugin.getWorkerThread().schedule(DamageTaken.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QDamageTaken t = QDamageTaken.damageTaken;

                    // For special entities which are clumped together
                    // currently only skeletons and wither skeletons fall under this category
                    EntityCode code = EntityCode.fromEntity(entity);

                    String entityValue;
                    if (entity instanceof Player) {
                        entityValue = String.valueOf(plugin.getDatabaseManager().getPlayerId(entity.getUniqueId()));
                    } else {
                        entityValue = entity.getName();
                    }

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);
                        if (clause == null)
                            return;
                        clause.columns(t.id, t.entity, t.type, t.amount)
                            .values(id, entityValue, code.getCode(), damageTaken).execute();
                    } catch (QueryException e) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);
                        if (clause == null)
                            return;
                        clause.where(
                            t.id.eq(id),
                            t.entity.eq(entityValue),
                            t.type.eq(code.getCode())
                        ).set(t.amount, t.amount.add(damageTaken)).execute();
                    }
                }
            });
        }
    }
}
