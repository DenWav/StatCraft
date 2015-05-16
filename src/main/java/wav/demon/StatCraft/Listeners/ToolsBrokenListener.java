package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import wav.demon.StatCraft.Querydsl.QToolsBroken;
import wav.demon.StatCraft.Querydsl.ToolsBroken;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class ToolsBrokenListener implements Listener {

    private StatCraft plugin;

    public ToolsBrokenListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final short item = (short) event.getBrokenItem().getType().getId();

        plugin.getWorkerThread().schedule(ToolsBroken.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QToolsBroken t = QToolsBroken.toolsBroken;

                if (query.from(t).where(t.id.eq(id).and(t.item.eq(item))).exists()) {
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(t);
                    clause.where(t.id.eq(id).and(t.item.eq(item))).set(t.amount, t.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(t);
                    clause.columns(t.id, t.item, t.amount).values(id, item, 1).execute();
                }
            }
        });
    }
}
