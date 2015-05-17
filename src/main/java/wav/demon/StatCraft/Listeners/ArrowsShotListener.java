package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import wav.demon.StatCraft.Magic.ArrowCode;
import wav.demon.StatCraft.Querydsl.ArrowsShot;
import wav.demon.StatCraft.Querydsl.QArrowsShot;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class ArrowsShotListener implements Listener {

    private StatCraft plugin;

    public ArrowsShotListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowShot(final EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            final UUID uuid = event.getEntity().getUniqueId();
            final ArrowCode code;
            if (event.getProjectile().getFireTicks() > 0)
                code = ArrowCode.FLAMING;
            else
                code = ArrowCode.NORMAL;

            plugin.getWorkerThread().schedule(ArrowsShot.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                    if (query == null)
                        return;
                    QArrowsShot a = QArrowsShot.arrowsShot;

                    if (query.from(a).where(a.id.eq(id).and(a.type.eq(code.getCode()))).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(a);
                        clause.where(
                            a.id.eq(id)
                            .and(a.type.eq(code.getCode()))
                        ).set(a.amount, a.amount.add(1)).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(a);
                        clause.columns(a.id, a.type, a.amount)
                            .values(id, code.getCode(), 1).execute();
                    }
                }
            });
        }
    }
}
