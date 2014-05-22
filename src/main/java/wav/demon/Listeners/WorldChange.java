package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class WorldChange extends StatListener {

    public WorldChange(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final String message = event.getFrom().getName() + ":" + event.getPlayer().getWorld().getName();

        incrementStat(StatTypes.WORLD_CHANGE.id, uuid, message);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of times a player has changed worlds
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.WORLD_CHANGE.id));
                String message = "§c" + name + "§f - World Changes: " + stat;
                respondToCommand(message, args, sender, StatTypes.WORLD_CHANGE);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - World Changes: 0", args, sender, StatTypes.WORLD_CHANGE);
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
        return "World Changes";
    }
}
