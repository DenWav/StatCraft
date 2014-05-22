package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class ToolsBroken extends StatListener {

    public ToolsBroken(StatCraft plugin) { super(plugin); }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToolBreak(PlayerItemBreakEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final String message = event.getBrokenItem().getType().getId() + ":" +
                event.getBrokenItem().getData().getData();

        incrementStat(StatTypes.TOOLS_BROKEN.id, uuid, message);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            try {
                String toolsBroken = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.TOOLS_BROKEN.id));
                String message = "§c" + name + "§f - Tools Broken: " + toolsBroken;
                respondToCommand(message, args, sender, StatTypes.TOOLS_BROKEN);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Tools Broken: 0", args, sender, StatTypes.TOOLS_BROKEN);
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
        return "Tools Broken";
    }
}
