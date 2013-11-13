package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft;

public class UpdateTotals implements CommandExecutor {
    StatCraft plugin;

    public UpdateTotals(StatCraft plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        plugin.getTimedActivities().forceTotalUpdate();
        return true;
    }
}
