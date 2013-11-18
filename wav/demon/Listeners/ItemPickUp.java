package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class ItemPickUp extends StatListener implements CommandExecutor {

    public ItemPickUp(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerPickupItemEvent event) {
        // FIXME: Need to find a different way to keep track of the items
        final String message = event.getItem().getItemStack().getData().toString();
        final String name = event.getPlayer().getName();
        incrementStat(StatTypes.ITEM_PICKUPS, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            long itemsPickedUp = getStat(name, StatTypes.ITEM_PICKUPS);

            sender.getServer().broadcastMessage(name + " - Items Picked Up: " + itemsPickedUp);
        }
        return true;
    }
}
