package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class BucketFill extends StatListener {

    public BucketFill(StatCraft plugin) { super(plugin); }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final String message = event.getItemStack().getTypeId() + "";
        incrementStat(StatTypes.FILL_BUCKET.id, uuid, message);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.FILL_BUCKET.id));
                String message = "§c" + name + "§f - Buckets Filled: " + stat;
                respondToCommand(message, args, sender, StatTypes.FILL_BUCKET);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Buckets Filled: 0", args, sender, StatTypes.FILL_BUCKET);
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
        return "Buckets Filled";
    }
}
