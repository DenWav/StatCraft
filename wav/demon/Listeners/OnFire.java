package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

public class OnFire extends StatListener implements CommandExecutor {

    public OnFire(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void oniFire(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player) {
            final String name = ((Player) event.getEntity()).getName();
            final int timeAdd = event.getDuration();
            final int currentTime = getStat(name, StatTypes.ON_FIRE.id);
            addStat(StatTypes.ON_FIRE.id, name, currentTime + timeAdd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        int fireTime;
        for (String name : names) {
            fireTime = getStat(name, StatTypes.ON_FIRE.id);

            String message = transformTime(fireTime);

            if (message.equalsIgnoreCase(""))
                sender.getServer().broadcastMessage(name + " doesn't have any logged time on fire yet.");
            else
                sender.getServer().broadcastMessage(name + " - Time on fire: " + message);
        }

        return true;
    }
}
