package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public final class DeathListener extends StatListener implements CommandExecutor {

    public DeathListener(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        final String message = event.getDeathMessage();
        final String name = event.getEntity().getName();
        incrementStat(StatTypes.DEATH.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of recorded deaths for a player
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        // otherwise, go through the array and print deaths for each player
        long deaths;
        for (String name : names) {
            deaths = getStat(name, StatTypes.DEATH.id);

            // print out the results
            sender.getServer().broadcastMessage(name + " - Deaths: " + deaths);
        }
        return true;
    }
}
