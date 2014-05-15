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
        String name = event.getPlayer().getName();
        if (getStat(name, StatTypes.HIGHEST_LEVEL.id) < newLevel) {
            addStat(StatTypes.HIGHEST_LEVEL.id, name, newLevel);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {

            int stat = getStat(name, StatTypes.HIGHEST_LEVEL.id);
            if (stat == 0)
                stat = sender.getServer().getPlayer(name).getLevel();

            String message = "§c" + name + "§f - Highest Level: " + df.format(stat);

            respondToCommand(message, args, sender, StatTypes.HIGHEST_LEVEL);

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

        if (getStat(player.getName(), StatTypes.HIGHEST_LEVEL.id) < level) {
            addStat(StatTypes.HIGHEST_LEVEL.id, player.getName(), level);
        }
    }
}
