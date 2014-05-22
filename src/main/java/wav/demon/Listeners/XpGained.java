package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class XpGained extends StatListener {

    public XpGained(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXpGain(PlayerExpChangeEvent event) {
        // I don't know if this is called when a player dies and loses exp or not
        int amount = event.getAmount();
        if (amount > 0) {
            String uuid = event.getPlayer().getUniqueId().toString();
            addStat(StatTypes.XP_GAINED.id, uuid, getStat(uuid, StatTypes.XP_GAINED.id) + amount);
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
                String stat = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.XP_GAINED.id));
                String message = "§c" + name + "§f - Exp Gained: " + stat;
                respondToCommand(message, args, sender, StatTypes.XP_GAINED);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Exp Gained: 0", args, sender, StatTypes.WORLD_CHANGE);
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
        return "Xp Gained";
    }
}
