package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class ToolsBroken extends StatListener {

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
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String toolsBroken = df.format(getStat(name, StatTypes.TOOLS_BROKEN.id));
            String message = "§c" + name + "§f - Tools Broken: " + toolsBroken;
            respondToCommand(message, args, sender);
        }
        return true;
    }
}
