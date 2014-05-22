package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class BucketEmpty extends StatListener {

    public BucketEmpty(StatCraft plugin) { super(plugin); }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final String message = event.getBucket().getId() + "";
        incrementStat(StatTypes.EMPTY_BUCKET.id, uuid, message);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.EMPTY_BUCKET.id));
                String message = "§c" + name + "§f - Buckets Emptied: " + stat;
                respondToCommand(message, args, sender, StatTypes.EMPTY_BUCKET);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Buckets Emptied: 0", args, sender, StatTypes.EMPTY_BUCKET);
            }
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Buckets Emptied";
    }
}
