package wav.demon.Listeners;

import com.google.common.base.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

/** BIG THANKS to Comphenix and Digi for figuring out a way to do this, I'm glad I didn't
 * have to figure this out myself.
 * https://forums.bukkit.org/threads/cant-get-amount-of-shift-click-craft-item.79090/ */
public class ItemsCrafted extends StatListener {

    public ItemsCrafted(StatCraft plugin) {
        super(plugin);
    }

    /** this method is Comphenix's code */
    @SuppressWarnings({"unused", "deprecation"})
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

                    final String message = toCraft.getType().getId() + ":" +
                            toCraft.getData().getData();
                    final String name = player.getName();

                    for (int y = 0; y < newItemsCount; y++)
                        incrementStat(StatTypes.ITEMS_CRAFTED.id, name, message);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String itemsCrafted = df.format(getStat(name, StatTypes.ITEMS_CRAFTED.id));
            String message = "§c" + name + "§f - Items Crafted: " + itemsCrafted;
            respondToCommand(message, args, sender, StatTypes.ITEMS_CRAFTED);
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return value + "";
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Items Crafted";
    }

    /** From here down is Comphenix's code */
    // HACK! The API doesn't allow us to easily determine the resulting number of
    // crafted items, so we're forced to compare the inventory before and after.
    @SuppressWarnings("deprecation")
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
                    final String message = compareItem.getType().getId() + ":" +
                            compareItem.getData().getData();
                    final String name = player.getName();

                    for (int y = 0; y < newItemsCount; y++)
                        incrementStat(StatTypes.ITEMS_CRAFTED.id, name, message);
                }
            }
        }, ticks);
    }

    @SuppressWarnings("deprecation")
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