package wav.demon.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import wav.demon.StatCraft;

public class BlockListener extends StatListener {

    public BlockListener(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        final String message = event.getBlock().toString();
        final String name = event.getPlayer().getName();
        addStat(2, name, message);
    }
}
