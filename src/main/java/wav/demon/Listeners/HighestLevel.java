package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class HighestLevel extends StatListener {

    public HighestLevel(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevel(PlayerLevelChangeEvent event) {
        int newLevel = event.getNewLevel();
        String uuid = event.getPlayer().getUniqueId().toString();
        if (getStat(uuid, StatTypes.HIGHEST_LEVEL.id) < newLevel) {
            addStat(StatTypes.HIGHEST_LEVEL.id, uuid, newLevel);
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
                int stat = getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.HIGHEST_LEVEL.id);
                if (stat == 0)
                    stat = sender.getServer().getPlayer(name).getLevel();

                String message = "§c" + name + "§f - Highest Level: " + df.format(stat);

                respondToCommand(message, args, sender, StatTypes.HIGHEST_LEVEL);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Highest Level: 0", args, sender, StatTypes.HIGHEST_LEVEL);
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
        return "Highest Level";
    }

    public void updateHighestLevel(Player player) {
        int level = player.getLevel();

        if (getStat(player.getUniqueId().toString(), StatTypes.HIGHEST_LEVEL.id) < level) {
            addStat(StatTypes.HIGHEST_LEVEL.id, player.getUniqueId().toString(), level);
        }
    }
}
