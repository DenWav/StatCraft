package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import wav.demon.StatCraft.Querydsl.BlockBreak;
import wav.demon.StatCraft.Querydsl.BlockPlace;
import wav.demon.StatCraft.Querydsl.QBlockBreak;
import wav.demon.StatCraft.Querydsl.QBlockPlace;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class BlockListener implements Listener {

    private StatCraft plugin;

    public BlockListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final short blockid = (short) event.getBlock().getTypeId();
        final short damage = event.getBlock().getData();
        final UUID uuid = event.getPlayer().getUniqueId();

        plugin.getWorkerThread().schedule(BlockBreak.class, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QBlockBreak b = QBlockBreak.blockBreak;

                if (query.from(b).where(
                        b.id.eq(id)
                        .and(b.blockid.eq(blockid))
                        .and(b.damage.eq(damage))
                    ).exists()) {

                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(b);
                    clause.where(
                        b.id.eq(id)
                        .and(b.blockid.eq(blockid))
                        .and(b.damage.eq(damage))
                    ).set(b.amount, b.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(b);
                    clause.columns(b.id, b.blockid, b.damage, b.amount)
                        .values(id, blockid, damage, 1).execute();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final short blockid = (short) event.getBlock().getTypeId();
        final short damage = event.getBlock().getData();
        final UUID uuid = event.getPlayer().getUniqueId();

        plugin.getWorkerThread().schedule(BlockPlace.class, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QBlockPlace b = QBlockPlace.blockPlace;

                if (query.from(b).where(
                    b.id.eq(id)
                        .and(b.blockid.eq(blockid))
                        .and(b.damage.eq(damage))
                ).exists()) {

                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(b);
                    clause.where(
                        b.id.eq(id)
                            .and(b.blockid.eq(blockid))
                            .and(b.damage.eq(damage))
                    ).set(b.amount, b.amount.add(1)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(b);
                    clause.columns(b.id, b.blockid, b.damage, b.amount)
                        .values(id, blockid, damage, 1).execute();
                }
            }
        });
    }
}
