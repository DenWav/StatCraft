package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft;

public class SaveStats implements CommandExecutor {

    StatCraft plugin;

    public SaveStats(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!plugin.getTimedActivities().statsToDiskNull()) {
            plugin.getTimedActivities().forceStatsToDisk();
        } else {
            commandSender.sendMessage("Delayed stat saving is disabled, stats are saved in real-time.");
        }
        return true;
    }
}
