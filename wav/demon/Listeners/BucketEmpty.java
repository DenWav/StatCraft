package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class BucketEmpty extends StatListener implements CommandExecutor {

    public BucketEmpty(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        final String name = event.getPlayer().getName();
        final String message = event.getBucket().getId() + "";
        incrementStat(StatTypes.EMPTY_BUCKET.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String stat = df.format(getStat(name, StatTypes.EMPTY_BUCKET.id));
            String message = name + " - Buckets Emptied: " + stat;
            respondToCommand(message, args, sender);
        }

        return true;
    }
}
