package wav.demon.StatCraft.Listeners;

import com.google.common.base.Objects;
import com.mysema.query.QueryException;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import wav.demon.StatCraft.Querydsl.ItemsCrafted;
import wav.demon.StatCraft.Querydsl.QItemsCrafted;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.util.UUID;

/** BIG THANKS to Comphenix and Digi for figuring out a way to do this, I'm glad I didn't
 *  have to figure this out myself.
 *  https://forums.bukkit.org/threads/cant-get-amount-of-shift-click-craft-item.79090/ */
public class ItemsCraftedListener implements Listener {

    StatCraft plugin;

    public ItemsCraftedListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    /** this method is Comphenix's code */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemCraft(final CraftItemEvent event) {
        HumanEntity player = event.getWhoClicked();
        ItemStack toCraft = event.getCurrentItem();
        ItemStack toStore = event.getCursor();

        // Make sure we are actually crafting anything
        if (player != null && hasItems(toCraft)) {
            if (event.isShiftClick()) {
                // Hack ahoy
                schedulePostDetection(player, toCraft);
            } else {
                // The items are stored in the cursor. Make sure there's enough space.
                if (isStackSumLegal(toCraft, toStore)) {
                    int newItemsCount = toCraft.getAmount();

                    final short item = (short) toCraft.getType().getId();
                    final short damage = toCraft.getData().getData();
                    final UUID uuid = player.getUniqueId();

                    updateData(item, damage, uuid, newItemsCount);
                }
            }
        }
    }

    private void updateData(final short itemid, short initDamage, final UUID uuid, final int amount) {
        final short damage = Util.damageValue(itemid, initDamage);
        plugin.getWorkerThread().schedule(ItemsCrafted.class, new Runnable() {
            @Override
            public void run() {
                int id = plugin.getDatabaseManager().getPlayerId(uuid);

                QItemsCrafted i = QItemsCrafted.itemsCrafted;

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

    /** From here down is Comphenix's code */
    // HACK! The API doesn't allow us to easily determine the resulting number of
    // crafted items, so we're forced to compare the inventory before and after.
    private void schedulePostDetection(final HumanEntity player, final ItemStack compareItem) {
        final ItemStack[] preInv = player.getInventory().getContents();
        final int ticks = 1;

        // Clone the array. The content may (was for me) be mutable.
        for (int i = 0; i < preInv.length; i++) {
            preInv[i] = preInv[i] != null ? preInv[i].clone() : null;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final ItemStack[] postInv = player.getInventory().getContents();
                int newItemsCount = 0;

                for (int i = 0; i < preInv.length; i++) {
                    ItemStack pre = preInv[i];
                    ItemStack post = postInv[i];

                    // We're only interested in filled slots that are different
                    if (hasSameItem(compareItem, post) && (hasSameItem(compareItem, pre) || pre == null)) {
                        newItemsCount += post.getAmount() - (pre != null ? pre.getAmount() : 0);
                    }
                }

                if (newItemsCount > 0) {
                    final short item = (short) compareItem.getType().getId();
                    final short damage = compareItem.getData().getData();
                    final UUID uuid = player.getUniqueId();

                    updateData(item, damage, uuid, newItemsCount);
                }
            }
        }, ticks);
    }

    private boolean hasSameItem(ItemStack a, ItemStack b) {
        if (a == null)
            return b == null;
        else if (b == null)
            return false;

        return a.getTypeId() == b.getTypeId() &&
               a.getDurability() == b.getDurability() &&
               Objects.equal(a.getData(), b.getData()) &&
               Objects.equal(a.getEnchantments(), b.getEnchantments());
    }

    private boolean isStackSumLegal(ItemStack a, ItemStack b) {
        // See if we can create a new item stack with the combined elements of a and b
        return a == null || b == null || a.getAmount() + b.getAmount() <= a.getType().getMaxStackSize();
    }

    private boolean hasItems(ItemStack stack) {
        return stack != null && stack.getAmount() > 0;
    }
}
