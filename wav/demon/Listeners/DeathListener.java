package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public final class DeathListener extends StatListener implements CommandExecutor {

    StatCraft plugin;

    public DeathListener(StatCraft plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        final String message = event.getDeathMessage();
        final String name = event.getEntity().getName();
        addStat(StatTypes.DEATH, name, message);
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
        }

        String[] names;
        if (args.length == 0)
            names = new String[] {sender.getName()};
        else
            names = args;

        // otherwise, go through the array and print deaths for each player
        int deaths;
        for (String name : names) {
            deaths = getStat(name, StatTypes.DEATH);

            // print out the results
            sender.getServer().broadcastMessage(name + " - Deaths: " + deaths);
        }
        return true;
    }
}
