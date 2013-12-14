package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class ArrowsShot extends StatListener implements CommandExecutor {

    public ArrowsShot(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowShot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            final String name = ((Player) event.getEntity()).getName();

            addStat(StatTypes.ARROWS_SHOT.id, name, getStat(name, StatTypes.ARROWS_SHOT.id) + 1);
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            sender.getServer().broadcastMessage(name + " - Arrows Shot: " + df.format(getStat(name, StatTypes.ARROWS_SHOT.id)));
        }
        return true;
    }
}
