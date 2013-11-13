package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResetCommand implements CommandExecutor {

    // TODO: make this where it can reset only a single person's stats, or a single person's type of stats

    private StatCraft plugin;

    public ResetCommand(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 1)
            return false;
        else if (strings[0].equalsIgnoreCase("force")) {
            File stats = new File("/opt/msm/servers/ocminecraft/stats.txt");
            stats.delete();
            plugin.statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
            return true;
        } else {
            return false;
        }
    }
}
