package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.expr.CaseBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import wav.demon.StatCraft.Magic.ProjectilesCode;
import wav.demon.StatCraft.Querydsl.Projectiles;
import wav.demon.StatCraft.Querydsl.QProjectiles;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class SnowballListener implements Listener {

    private StatCraft plugin;

    public SnowballListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSnowball(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) event.getEntity().getShooter();
            final Location playerLocation = player.getLocation();
            final Location snowballLocation = event.getEntity().getLocation();

            final double distance = playerLocation.distance(snowballLocation);
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
                            .values(id, ProjectilesCode.SNOWBALL.getCode(), 1, finalDistance, finalDistance).execute();
                    } catch (QueryException e) {
                        // UPDATE
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(p);

                        if (clause == null)
                            return;

                        clause.where(p.id.eq(id), p.type.eq(ProjectilesCode.SNOWBALL.getCode()))
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
