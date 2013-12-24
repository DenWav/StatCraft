package wav.demon.Listeners;

import com.google.common.collect.Ordering;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.util.*;

public class BlockListener extends StatListener {

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

                message = type.toString();

                if (message != null) {
                    for (int x = 1; x <= stack.getAmount(); x++) {
                        incrementStat(StatTypes.MINED.id, name, message);
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

                String message = "§c" + name + "§f - Blocks Broken: " + blocksBroken + " Blocks Placed: " + blocksPlaced;

                // print out the results
                respondToCommand(message, args, sender, null);
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("mined")) {
            // list ores mined by a player
            if (args.length > 3 || args.length <= 1)
                return false;

            String name = args[0];
            String type = args[1];
            String message;

            int stat = getStat(name, StatTypes.MINED.id, type);
            String material = type.replace("_", " ");
            if (stat != -1) {
                message = "§c" + name + "§f - " + WordUtils.capitalizeFully(material) + " Mined: " + df.format(stat);
            } else {
                message = "§c" + name + "§f - " + WordUtils.capitalizeFully(material) + " Mined: " + 0;
            }

            respondToCommand(message, args, sender, null);

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return value + "";
    }

    @Override
    protected String typeLabel(StatTypes type) {
        if (type == StatTypes.BLOCK_BREAK)
            return "Blocks Broken";
        else
            return "Blocks Placed";
    }

    private int getStat(String name, int type, String s) {
        Material mat = Material.matchMaterial(s);
        if (mat != null) {
            int stat;
            if (plugin.statsForPlayers.containsKey(name))
                if (plugin.statsForPlayers.get(name).containsKey(type))
                    if (plugin.statsForPlayers.get(name).get(type).containsKey(mat.toString()))
                        stat = plugin.statsForPlayers.get(name).get(type).get(mat.toString());
                    else
                        stat = 0;
                else
                    stat = 0;
            else
                stat = 0;

            return stat;
        } else {
            return -1;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void respondToCommand(String message, String[] args, CommandSender sender, StatTypes type) {
        boolean publicCmd = false;
        boolean top = false;
        int topNumber = 0;

        for (String arg : args) {
            if (arg.equals("-all"))
                publicCmd = true;

            if (arg.startsWith("-top")) {
                top = true;
                try {
                    topNumber = Integer.valueOf(arg.replace("-top", ""));
                } catch (NumberFormatException e) {
                    sender.sendMessage("Not a valid \"-top\" value. Please use \"-top#\" with # being an integer.");
                    return;
                }
            }
        }

        if (!top) {
            if (publicCmd)
                sender.getServer().broadcastMessage("§3@" + sender.getName() + "§f: " + message);
            else
                sender.sendMessage(message);
        } else {
            Map<String, Integer> sortableBlocksPlaced = Collections.synchronizedMap(new ValueComparableMap<String, Integer>(Ordering.from(Collections.reverseOrder())));
            Map<String, Integer> sortableBlocksBroken = Collections.synchronizedMap(new ValueComparableMap<String, Integer>(Ordering.from(Collections.reverseOrder())));

            for (Map.Entry<String, HashMap<Integer, HashMap<String, Integer>>> pairs : plugin.statsForPlayers.entrySet()) {
                String name = pairs.getKey();
                if (!name.equalsIgnoreCase("total")) {
                    HashMap<Integer, HashMap<String, Integer>> playerMap = pairs.getValue();
                    Map<String, Integer> blocksPlacedMap = playerMap.get(StatTypes.BLOCK_PLACE.id);
                    Map<String, Integer> blocksBrokenMap = playerMap.get(StatTypes.BLOCK_BREAK.id);

                    if (blocksPlacedMap != null) {
                        Integer total = blocksPlacedMap.get("total");
                        if (total != null) {
                            sortableBlocksPlaced.put(name, total);
                        }
                    }

                    if (blocksBrokenMap != null) {
                        Integer total = blocksBrokenMap.get("total");
                        if (total != null) {
                            sortableBlocksBroken.put(name, total);
                        }
                    }
                }
            }

            String blocksBrokenOutput = typeLabel(StatTypes.BLOCK_BREAK) + " - ";
            Iterator brokenIt = sortableBlocksBroken.entrySet().iterator();
            for (int i = 1; i <= topNumber; i++) {
                if (brokenIt.hasNext()) {
                    Map.Entry<String, Integer> sortedMapEntry = (Map.Entry<String, Integer>) brokenIt.next();

                    String name = sortedMapEntry.getKey();
                    Integer value = sortedMapEntry.getValue();

                    blocksBrokenOutput = blocksBrokenOutput + "§6" + i + ". §c" + name + "§f: " + typeFormat(value, type) + " ";
                } else {
                    break;
                }
            }

            String blocksPlacedOutput = typeLabel(StatTypes.BLOCK_PLACE) + " - ";
            Iterator placedIt = sortableBlocksPlaced.entrySet().iterator();
            for (int i = 1; i <= topNumber; i++) {
                if (placedIt.hasNext()) {
                    Map.Entry<String, Integer> sortedMapEntry = (Map.Entry<String, Integer>) placedIt.next();

                    String name = sortedMapEntry.getKey();
                    Integer value = sortedMapEntry.getValue();

                    blocksPlacedOutput = blocksPlacedOutput + "§6" + i + ". §c" + name + "§f: " + typeFormat(value, type) + " ";
                } else {
                    break;
                }
            }

            if (publicCmd) {
                sender.getServer().broadcastMessage("§3@" + sender.getName() + "§f: " + blocksBrokenOutput);
                sender.getServer().broadcastMessage("§3@" + sender.getName() + "§f: " + blocksPlacedOutput);
            } else {
                sender.sendMessage(blocksBrokenOutput);
                sender.sendMessage(blocksPlacedOutput);
            }
        }
    }
}