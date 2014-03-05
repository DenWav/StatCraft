package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class TabComplete extends StatListener {

    public TabComplete(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTabComplete(PlayerChatTabCompleteEvent event) {
        String name = event.getPlayer().getName();

        addStat(StatTypes.TAB_COMPLETE.id, name, getStat(name, StatTypes.TAB_COMPLETE.id) + 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String stat = df.format(getStat(name, StatTypes.TAB_COMPLETE.id));
            String message = "§c" + name + "§f - Tab Completes: " + stat;
            respondToCommand(message, args, sender, StatTypes.TAB_COMPLETE);
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Tab Completes";
    }
}
