package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wav.demon.StatCraft.Querydsl.OnFire;
import wav.demon.StatCraft.Querydsl.QOnFire;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class OnFireListener implements Listener {

    private StatCraft plugin;

    public OnFireListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFire(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

                final UUID uuid = event.getEntity().getUniqueId();

                plugin.getWorkerThread().schedule(OnFire.class, new Runnable() {
                    @Override
                    public void run() {
                        int id = plugin.getDatabaseManager().getPlayerId(uuid);

                        QOnFire o = QOnFire.onFire;

                        try {
                            // INSERT
                            SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(o);

                            if (clause == null)
                                return;

                            clause.columns(o.id, o.time).values(id, 1).execute();
                        } catch (QueryException e) {
                            // UPDATE
                            SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(o);

                            if (clause == null)
                                return;

                            clause.where(o.id.eq(id)).set(o.time, o.time.add(1)).execute();
                        }
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        if (plugin.config().stats.on_fire_announce)
        if (event.getEntity() instanceof Player) {
            UUID uuid = event.getEntity().getUniqueId();
            if ((System.currentTimeMillis() / 1000) - plugin.getLastFireTime(uuid) > 60) {
                boolean giveWarning = true;
                for (PotionEffect pe : ((Player) event.getEntity()).getActivePotionEffects()) {
                    if (pe.getType().getName().equalsIgnoreCase(PotionEffectType.FIRE_RESISTANCE.getName()))
                        giveWarning = false;
                }
                if (giveWarning) {
                    event.getEntity().getServer().broadcastMessage("§c" + event.getEntity().getName() + " is on fire! Oh no!");
                    plugin.setLastFireTime(uuid, (int) (System.currentTimeMillis() / 1000));
                }
            }
        }
    }
}
