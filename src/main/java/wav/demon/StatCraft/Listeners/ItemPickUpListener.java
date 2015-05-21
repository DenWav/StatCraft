package wav.demon.StatCraft.Listeners;

import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import wav.demon.StatCraft.Querydsl.ItemPickups;
import wav.demon.StatCraft.Querydsl.QItemPickups;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.UUID;

public class ItemPickUpListener implements Listener {

    private StatCraft plugin;

    public ItemPickUpListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        final short itemid = (short) event.getItem().getItemStack().getTypeId();
        final short damage = Util.damageValue(itemid, event.getItem().getItemStack().getData().getData());
        final UUID uuid = event.getPlayer().getUniqueId();
        final int amount = event.getItem().getItemStack().getAmount();

        plugin.getThreadManager().schedule(ItemPickups.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QItemPickups i = QItemPickups.itemPickups;

                try {
                    // INSERT
                    SQLInsertClause clause = plugin.getDatabaseManager().getInsertClause(i);

                    if (clause == null)
                        return;

                    clause.columns(i.id, i.item, i.damage, i.amount)
                        .values(id, itemid, damage, amount).execute();
                } catch (QueryException e) {
                    // UPDATE
                    SQLUpdateClause clause = plugin.getDatabaseManager().getUpdateClause(i);

                    if (clause == null)
                        return;

                    clause.where(
                        i.id.eq(id),
                        i.item.eq(itemid),
                        i.damage.eq(damage)
                    ).set(i.amount, i.amount.add(amount)).execute();
                }
            }
        });
    }
}
