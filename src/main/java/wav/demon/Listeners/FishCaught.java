package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.ArrayList;

public class FishCaught extends StatListener {

    public FishCaught(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            final String name = event.getPlayer().getName();
            if (event.getCaught() instanceof Item) {
                Item item = (Item) event.getCaught();
                item.getItemStack().getData();

                String message;

                switch (item.getItemStack().getType()) {
                    case RAW_FISH:
                        message = "fish";
                        break;
                    case BOW:
                    case ENCHANTED_BOOK:
                    case NAME_TAG:
                    case SADDLE:
                    case WATER_LILY:
                        message = "treasure";
                        break;
                    case BOWL:
                    case LEATHER:
                    case LEATHER_BOOTS:
                    case ROTTEN_FLESH:
                    case STICK:
                    case STRING:
                    case GLASS_BOTTLE:
                    case BONE:
                    case INK_SACK:
                    case TRIPWIRE_HOOK:
                        message = "junk";
                        break;
                    case FISHING_ROD:
                        if (item.getItemStack().getEnchantments() == null)
                            message = "junk";
                        else
                            message = "treasure";
                        break;
                    default:
                        message = "other";
                        break;
                }

                incrementStat(StatTypes.FISH_CAUGHT.id, name, message);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> names = getPlayers(sender, args);
        if (names == null)
            return false;

        for (String name : names) {
            String fishCaught = df.format(getStat(name, StatTypes.FISH_CAUGHT.id, "fish"));
            String junkCaught = df.format(getStat(name, StatTypes.FISH_CAUGHT.id, "junk"));
            String treasureCaught = df.format(getStat(name, StatTypes.FISH_CAUGHT.id, "treasure"));
            String message = "§c" + name + "§f - Fish Caught: " + fishCaught + " | Treasure Caught: " + treasureCaught +
                    " | Junk Caught: " + junkCaught;
            respondToCommand(message, args, sender, StatTypes.FISH_CAUGHT);
        }

        return true;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Fish Caught";
    }

    private int getStat(String name, int type, String s) {
        int stat;
        if (plugin.statsForPlayers.containsKey(name))
            if (plugin.statsForPlayers.get(name).containsKey(type))
                if (plugin.statsForPlayers.get(name).get(type).containsKey(s))
                    stat = plugin.statsForPlayers.get(name).get(type).get(s);
                else
                    stat = 0;
            else
                stat = 0;
        else
            stat = 0;

        return stat;
    }
}
