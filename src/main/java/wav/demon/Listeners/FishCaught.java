package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FishCaught extends StatListener {

    public FishCaught(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            final String uuid = event.getPlayer().getUniqueId().toString();
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

                incrementStat(StatTypes.FISH_CAUGHT.id, uuid, message);
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
                String fishCaught = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.FISH_CAUGHT.id, "fish"));
                String junkCaught = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.FISH_CAUGHT.id, "junk"));
                String treasureCaught = df.format(getStat(name, StatTypes.FISH_CAUGHT.id, "treasure"));
                String message = "§c" + name + "§f - Fish Caught: " + fishCaught + " | Treasure Caught: " + treasureCaught +
                        " | Junk Caught: " + junkCaught;
                respondToCommand(message, args, sender, StatTypes.FISH_CAUGHT);
            } catch (NullPointerException e) {
                respondToCommand("§c" + name + "§f - Fish Caught: 0" + " | Treasure Caught: 0" +
                        " | Junk Caught: 0", args, sender, StatTypes.FISH_CAUGHT);
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
        return "Fish Caught";
    }

    private int getStat(String uuid, int type, String s) {
        // This is method of getting stats takes about half as many look-ups
        // I could do a little better, but then I'd have to catch NullPointedExceptions,
        // and I would rather not catch a RuntimeException if possible
        if (plugin.getSaveStatsRealTime()) {
            Integer i = getStatFromFile(uuid, type, s);
            return i == null ? 0 : i;
        } else {
            HashMap<Integer, HashMap<String, Integer>> firstMap = plugin.statsForPlayers.get(uuid);
            if (firstMap == null) {
                Integer i = getStatFromFile(uuid, type, s);
                return i == null ? 0 : i;
            } else {
                HashMap<String, Integer> secondMap = firstMap.get(type);
                if (secondMap == null) {
                    Integer i = getStatFromFile(uuid, type, s);
                    return i == null ? 0 : i;
                } else {
                    Integer i = secondMap.get(s);
                    return i == null ? 0 : i;
                }
            }
        }
    }

    private Integer getStatFromFile(String uuid, int type, String s) {

        File statFile = new File(plugin.getDataFolder(), "stats/" + uuid + "/" + type);

        HashMap<String, Integer> map = getMapFromFile(statFile);

        return map == null ? null : map.containsKey("total") ? map.get(s) : null;
    }
}
