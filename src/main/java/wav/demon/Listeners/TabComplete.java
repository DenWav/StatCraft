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

    public TabComplete(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTabComplete(PlayerChatTabCompleteEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();

        addStat(StatTypes.TAB_COMPLETE.id, uuid, getStat(uuid, StatTypes.TAB_COMPLETE.id) + 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.TAB_COMPLETE.id));
                String message = "§c" + name + "§f - Tab Completes: " + stat;
                respondToCommand(message, args, sender, StatTypes.TAB_COMPLETE);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Tab Completes: 0", args, sender, StatTypes.TAB_COMPLETE);
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
        return "Tab Completes";
    }
}
