package wav.demon.Listeners;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class BlockListener extends StatListener implements CommandExecutor {

    public BlockListener(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        String message = event.getBlock().getType().getId() + ":" + event.getBlock().getData();
        final String name = event.getPlayer().getName();
        incrementStat(StatTypes.BLOCK_BREAK.id, name, message);

        if (plugin.getMined_ores()) {
            Block block = event.getBlock();
            Material type = block.getType();
            Collection<ItemStack> drops = block.getDrops(event.getPlayer().getItemInHand());
            for (ItemStack stack : drops) {
                switch (type) {
                    case COAL_ORE:
                        message = Ores.COAL.id + "";
                        break;
                    case IRON_ORE:
                        message = Ores.IRON.id + "";
                        break;
                    case GOLD_ORE:
                        message = Ores.GOLD.id + "";
                        break;
                    case DIAMOND_ORE:
                        message = Ores.DIAMOND.id + "";
                        break;
                    case REDSTONE_ORE:
                        message = Ores.REDSTONE.id + "";
                        break;
                    case LAPIS_ORE:
                        message = Ores.LAPIS.id + "";
                        break;
                    case EMERALD_ORE:
                        message = Ores.EMERALD.id + "";
                        break;
                    default:
                        message = "";
                        break;
                }

                if (!message.equals("")) {
                    for (int x = 1; x <= stack.getAmount(); x++) {
                        incrementStat(StatTypes.MINED_ORES.id, name, message);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final String message = event.getBlock().getType().getId() + ":" + event.getBlock().getData();
        final String name = event.getPlayer().getName();
        incrementStat(StatTypes.BLOCK_PLACE.id, name, message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("blocks")) {
            // list the number of recorded deaths for a player
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                String blocksBroken = df.format(super.getStat(name, StatTypes.BLOCK_BREAK.id));
                String blocksPlaced = df.format(super.getStat(name, StatTypes.BLOCK_PLACE.id));

                String message = name + " - Blocks Broken: " + blocksBroken + " Blocks Placed: " + blocksPlaced;

                // print out the results
                respondToCommand(message, args, sender);
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("mined")) {
            // list ores mined by a player
            if (args.length > 3 && args.length < 1)
                return false;

            String name = args[0];
            String type = args[1];
            String message;

            int stat = getStat(name, StatTypes.MINED_ORES.id, type);
            if (stat != -1) {
                message = name + " - " + WordUtils.capitalize(type) + " Mined: " + df.format(stat);
            } else {
                message = name + " - " + WordUtils.capitalize(type) + " Mined: " + 0;
            }

            respondToCommand(message, args, sender);

            return true;
        } else {
            return false;
        }
    }

    protected int getStat(String name, int type, String ore) {
        Ores oreType = Ores.getOreFromName(ore);
        if (oreType != null) {
            int stat;
            if (plugin.statsForPlayers.containsKey(name))
                if (plugin.statsForPlayers.get(name).containsKey(type))
                    if (plugin.statsForPlayers.get(name).get(type).containsKey(oreType.id + ""))
                        stat = plugin.statsForPlayers.get(name).get(type).get(oreType.id + "");
                    else
                        stat = 0;
                else
                    stat = 0;
            else
                stat = 0;

            System.out.println(stat);
            return stat;
        } else {
            return -1;
        }
    }
}

enum Ores {

    COAL    (1, "coal"),
    IRON    (2, "iron"),
    GOLD    (3, "gold"),
    DIAMOND (4, "diamond"),
    REDSTONE(5, "redstone"),
    LAPIS   (6, "lapis"),
    EMERALD (7, "emerald");

    public final int id;
    public final String name;

    Ores(int id, String name) {

        this.id = id;
        this.name = name;

    }

    @Nullable
    public static Ores getOreFromName(String name) {

        Ores answer = null;
        for (Ores o : Ores.values()) {
            if (o.name.equals(name)) {
                answer = o;
            }
        }

        return answer;
    }

}