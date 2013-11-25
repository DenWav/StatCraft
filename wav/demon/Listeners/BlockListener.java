package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class BlockListener extends StatListener implements CommandExecutor {

    StatCraft plugin;

    public BlockListener(StatCraft plugin) {
        super(plugin);
        this.plugin = plugin;
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
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        long blocksBroken;
        long blocksPlaced;

        for (String name : names) {
            blocksBroken = getStat(name, StatTypes.BLOCK_BREAK.id);
            blocksPlaced = getStat(name, StatTypes.BLOCK_PLACE.id);

            // print out the results
            sender.getServer().broadcastMessage(name + " - Blocks Broken: " + blocksBroken +
                    " Blocks Placed: " + blocksPlaced);
        }
        return true;
    }
}
