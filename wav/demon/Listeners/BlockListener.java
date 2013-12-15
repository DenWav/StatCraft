package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class BlockListener extends StatListener implements CommandExecutor {

    public BlockListener(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final String message = event.getBlock().getType().getId() + ":" + event.getBlock().getData();
        final String name = event.getPlayer().getName();
        incrementStat(StatTypes.BLOCK_BREAK.id, name, message);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final String message = event.getBlock().getType().getId() + ":" + event.getBlock().getData();
        final String name = event.getPlayer().getName();
        incrementStat(StatTypes.BLOCK_PLACE.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of recorded deaths for a player
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String blocksBroken = df.format(getStat(name, StatTypes.BLOCK_BREAK.id));
            String blocksPlaced = df.format(getStat(name, StatTypes.BLOCK_PLACE.id));

            String message = name + " - Blocks Broken: " + blocksBroken + " Blocks Placed: " + blocksPlaced;

            // print out the results
            respondToCommand(message, args, sender);
        }
        return true;
    }
}
