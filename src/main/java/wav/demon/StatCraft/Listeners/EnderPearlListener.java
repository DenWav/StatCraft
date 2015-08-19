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

import wav.demon.StatCraft.Magic.ProjectilesCode;
import wav.demon.StatCraft.Querydsl.Projectiles;
import wav.demon.StatCraft.Querydsl.QProjectiles;
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

            final UUID uuid = player.getUniqueId();

            plugin.getThreadManager().schedule(Projectiles.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    QProjectiles p = QProjectiles.projectiles;

                    try {
                        // INSERT
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(p);

                        if (clause == null)
                            return;

                        clause.columns(p.id, p.type, p.amount, p.totalDistance, p.maxThrow)
                            .values(id, ProjectilesCode.ENDER_PEARL.getCode(), 1, finalDistance, finalDistance).execute();
                    } catch (QueryException ex) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(p);

                        if (clause == null)
                            return;

                        clause.where(p.id.eq(id), p.type.eq(ProjectilesCode.ENDER_PEARL.getCode()))
                            .set(p.amount, p.amount.add(1))
                            .set(p.totalDistance, p.totalDistance.add(finalDistance))
                            .set(p.maxThrow,
                                new CaseBuilder()
                                    .when(p.maxThrow.lt(finalDistance)).then(finalDistance)
                                    .otherwise(p.maxThrow))
                            .execute();
                    }
                }
            });
        }
    }
}
