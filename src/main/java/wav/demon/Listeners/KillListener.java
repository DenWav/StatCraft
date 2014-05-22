package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class KillListener extends StatListener {

    public KillListener(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            String uuid = event.getEntity().getKiller().getUniqueId().toString();
            if (event.getEntity() instanceof Player) {
                incrementStat(StatTypes.KILLS.id, uuid, ((Player) event.getEntity()).getName());
            } else {
                incrementStat(StatTypes.KILLS.id, uuid, event.getEntity().getType().toString());
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.KILLS.id));
                String message = "§c" + name + "§f - Kills: " + stat;
                respondToCommand(message, args, sender, StatTypes.KILLS);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Kills: 0", args, sender, StatTypes.KILLS);
            }
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Kills";
    }
}
