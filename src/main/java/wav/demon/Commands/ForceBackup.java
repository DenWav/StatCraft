package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft;

public class ForceBackup implements CommandExecutor{

    StatCraft plugin;

    public ForceBackup(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!plugin.getTimedActivities().backupNull()) {
            plugin.getTimedActivities().forceBackup();
        } else {
            commandSender.sendMessage("Backups are disabled.");
        }
        return true;
    }
}
