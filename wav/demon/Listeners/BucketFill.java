package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class BucketFill extends StatListener implements CommandExecutor {

    public BucketFill(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        final String name = event.getPlayer().getName();
        final String message = event.getItemStack().getTypeId() + "";
        incrementStat(StatTypes.FILL_BUCKET.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            sender.getServer().broadcastMessage(name + " - Buckets Filled: " + getStat(name, StatTypes.FILL_BUCKET.id));
        }

        return true;
    }
}
