package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class WorldChange extends StatListener implements CommandExecutor {

    public WorldChange(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        final String name = event.getPlayer().getName();
        final String message = event.getFrom().getName() + ":" + event.getPlayer().getWorld().getName();

        incrementStat(StatTypes.WORLD_CHANGE.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of times a player has changed worlds
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            sender.getServer().broadcastMessage(name + " - World Changes: " + getStat(name, StatTypes.WORLD_CHANGE.id));
        }
        return true;
    }
}
