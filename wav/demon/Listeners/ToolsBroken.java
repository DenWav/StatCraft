package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class ToolsBroken extends StatListener implements CommandExecutor {

    public ToolsBroken(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        final String name = event.getPlayer().getName();
        final String message = event.getBrokenItem().getType().getId() + ":" +
                event.getBrokenItem().getData().getData();

        incrementStat(StatTypes.TOOLS_BROKEN.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            int toolsBroken = getStat(name, StatTypes.TOOLS_BROKEN.id);

            sender.getServer().broadcastMessage(name + " - Tools Broken: " + toolsBroken);
        }
        return true;
    }
}
