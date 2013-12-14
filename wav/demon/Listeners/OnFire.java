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

            addStat(StatTypes.ON_FIRE.id, name, getStat(name, StatTypes.ON_FIRE.id) + 1);

            // Currently getting the time an entity was actually on fire is impossible, as far as I can tell
            // the getDuration() method only specifies how long an entity *should* be on fire, not how long it actually
            // is on fire, from the entity jumping into water, dying, or whatever. Until I can figure out a way to do
            // this, I will just keep track of how many times the entity has been on fire.

            // final int timeAdd = event.getDuration();
            // final int currentTime = getStat(name, StatTypes.ON_FIRE.id);
            // addStat(StatTypes.ON_FIRE.id, name, currentTime + timeAdd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String[] names = getPlayers(sender, args);
        if (names == null)
            return false;

        //int fireTime;
        for (String name : names) {
            sender.getServer().broadcastMessage(name + " - On Fire: " + df.format(getStat(name, StatTypes.ON_FIRE.id)));
            // fireTime = getStat(name, StatTypes.ON_FIRE.id);

            // String message = transformTime(fireTime);

            // if (message.equalsIgnoreCase(""))
            //     sender.getServer().broadcastMessage(name + " doesn't have any logged time on fire yet.");
            // else
             //    sender.getServer().broadcastMessage(name + " - Time on fire: " + message);
        }

        return true;
    }
}
