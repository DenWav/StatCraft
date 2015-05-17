package wav.demon.StatCraft.Listeners;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import wav.demon.StatCraft.Querydsl.ItemPickups;
import wav.demon.StatCraft.Querydsl.QItemPickups;
import wav.demon.StatCraft.StatCraft;

import java.util.UUID;

public class ItemPickUpListener implements Listener {

    private StatCraft plugin;

    public ItemPickUpListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        final short item = (short) event.getItem().getItemStack().getTypeId();
        final short damage = event.getItem().getItemStack().getData().getData();
        final UUID uuid = event.getPlayer().getUniqueId();
        final int amount = event.getItem().getItemStack().getAmount();

        plugin.getWorkerThread().schedule(ItemPickups.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                SQLQuery query = plugin.getDatabaseManager().getNewQuery();
                if (query == null)
                    return;
                QItemPickups i = QItemPickups.itemPickups;

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
