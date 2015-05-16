package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import wav.demon.StatCraft.Querydsl.ItemDrops;
import wav.demon.StatCraft.Querydsl.QItemDrops;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class ItemDropListener implements Listener {

    StatCraft plugin;

    public ItemDropListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final short item = (short) event.getItemDrop().getItemStack().getTypeId();
        final short damage = event.getItemDrop().getItemStack().getData().getData();
        final int amount = event.getItemDrop().getItemStack().getAmount();

        plugin.getWorkerThread().schedule(ItemDrops.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                QItemDrops i = QItemDrops.itemDrops;

                if (query.from(i).where(
                    i.id.eq(id)
                        .and(i.item.eq(item))
                        .and(i.damage.eq(damage))
                ).exists()) {

                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(i);
                    clause.where(
                        i.id.eq(id)
                            .and(i.item.eq(item))
                            .and(i.damage.eq(damage))
                    ).set(i.amount, i.amount.add(amount)).execute();
                } else {
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(i);
                    clause.columns(i.id, i.item, i.damage, i.amount)
                        .values(id, item, damage, amount).execute();
                }
            }
        });
    }
}
