package wav.demon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCraftDeathsCommandExecutor implements CommandExecutor {

    private StatCraft plugin;

    public StatCraftDeathsCommandExecutor(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of recorded deaths for a player
        // first, figure out which player to list deaths for
        if (!(sender instanceof Player)) {
            // if this is run from the console, then a player name must be provided
            if (args.length == 0) {
                // tell them to provide only one name and print usage
                sender.sendMessage("You must name someone to list deaths for from the console!");
                return false;
            }
        } else {
            // if no arguments were given from a player, simply use him as the name
            int deaths;
            if (args.length == 0) {
                String name = sender.getName();
                deaths = plugin.getMap().get(name).get("death").get("total");
                if (deaths == 1) {
                    sender.getServer().broadcastMessage(name + " has died " + deaths + " time.");
                } else {
                    sender.getServer().broadcastMessage(name + " has died " + deaths + " times.");
                }
                return true;
            }
        }
        // otherwise, go through the array and print deaths for each player
        int deaths;
        for (String name : args) {
            deaths = plugin.getMap().get(name).get("death").get("total");
            if (deaths == 1) {
                sender.getServer().broadcastMessage(name + " has died " + deaths + " time.");
            } else {
                sender.getServer().broadcastMessage(name + " has died " + deaths + " times.");
            }
        }
        return true;
    }
}
