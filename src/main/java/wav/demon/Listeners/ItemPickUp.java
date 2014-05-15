package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class ItemPickUp extends StatListener {

    public ItemPickUp(StatCraft plugin) { super(plugin); }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        final String message = event.getItem().getItemStack().getType().getId() + ":" +
                event.getItem().getItemStack().getData().getData();
        final String name = event.getPlayer().getName();
        final int x = event.getItem().getItemStack().getAmount();

        for (int y = 0; y < x; y++)
            incrementStat(StatTypes.ITEM_PICKUPS.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String itemsPickedUp = df.format(getStat(name, StatTypes.ITEM_PICKUPS.id));
            String message = "§c" + name + "§f - Items Picked Up: " + itemsPickedUp;
            respondToCommand(message, args, sender, StatTypes.ITEM_PICKUPS);
        }
        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Items Picked Up";
    }
}
