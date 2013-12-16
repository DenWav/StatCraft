package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class FishCaught extends StatListener implements CommandExecutor {

    public FishCaught(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            final String name = event.getPlayer().getName();

            addStat(StatTypes.FISH_CAUGHT.id, name, getStat(name, StatTypes.FISH_CAUGHT.id) + 1);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String stat = df.format(getStat(name, StatTypes.FISH_CAUGHT.id));
            String message = name + " - Fish Caught: " + stat;
            respondToCommand(message, args, sender);
        }

        return true;
    }
}
