package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import wav.demon.StatCraft.Querydsl.QXpGained;
import wav.demon.StatCraft.Querydsl.XpGained;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class XpGainedListener implements Listener {

    private StatCraft plugin;

    public XpGainedListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXpGain(PlayerExpChangeEvent event) {
        final int amount = event.getAmount();
        if (amount > 0) {
            final UUID uuid = event.getPlayer().getUniqueId();

            plugin.getWorkerThread().schedule(XpGained.class, new Runnable() {
                @Override
                public void run() {
                    int id = plugin.getDatabaseManager().getPlayerId(uuid);

                    SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                    if (query == null)
                        return;
                    QXpGained x = QXpGained.xpGained;

                    if (query.from(x).where(x.id.eq(id)).exists()) {
                        SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(x);
                        clause.where(x.id.eq(id)).set(x.amount, x.amount.add(amount)).execute();
                    } else {
                        SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(x);
                        clause.columns(x.id, x.amount).values(id, amount).execute();
                    }
                }
            });
        }
    }
}
