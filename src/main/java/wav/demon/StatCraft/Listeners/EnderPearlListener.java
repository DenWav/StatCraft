package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.expr.CaseBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import wav.demon.StatCraft.Querydsl.EnderPearls;
import wav.demon.StatCraft.Querydsl.QEnderPearls;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class EnderPearlListener implements Listener {

    private StatCraft plugin;

    public EnderPearlListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderPearl(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            final Player player = event.getPlayer();
            final Location toLocation = event.getTo();
            final Location fromLocation = event.getFrom();

            final double distance = fromLocation.distance(toLocation);
            final int finalDistance = (int) Math.round(distance * 100.0);

            plugin.getLogger().info(distance + "");
            plugin.getLogger().info(finalDistance + "");

            final UUID uuid = player.getUniqueId();

            plugin.getWorkerThread().schedule(EnderPearls.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QEnderPearls e = QEnderPearls.enderPearls;

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(e);

                        if (clause == null)
                            return;

                        clause.columns(e.id, e.amount, e.distance, e.maxThrow)
                            .values(id, 1, finalDistance, finalDistance).execute();
                    } catch (QueryException ex) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(e);

                        if (clause == null)
                            return;

                        clause.where(e.id.eq(id))
                            .set(e.amount, e.amount.add(1))
                            .set(e.distance, e.distance.add(finalDistance))
                            .set(e.maxThrow,
                                new CaseBuilder()
                                    .when(e.maxThrow.lt(finalDistance)).then(finalDistance)
                                    .otherwise(e.maxThrow))
                            .execute();
                    }
                }
            });
        }
    }
}
