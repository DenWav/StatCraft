package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeathListener extends StatListener {

    public DeathListener(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        final String message = event.getDeathMessage();
        final String uuid = event.getEntity().getUniqueId().toString();
        incrementStat(StatTypes.DEATH.id, uuid, message);

        if (plugin.getDeath_locations())
            incrementStat(StatTypes.DEATH_LOCATIONS.id, uuid, event.getEntity().getWorld().getName());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("deaths")) {
            // list the number of recorded deaths for a player
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                try {
                    String deaths = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.DEATH.id));
                    String message = "§c" + name + "§f - Deaths: " + deaths;

                    // print out the results
                    respondToCommand(message, args, sender, StatTypes.DEATH);
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f - Deaths: 0", args, sender, StatTypes.DEATH);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("deathlocations")) {
            // list the number of recorded deaths and their locations for a player
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String arg : args) {
                if (arg.startsWith("-top")) {
                    sender.sendMessage("\"-top\" is not allowed with the deathlocations command.");

                    return true;
                }
            }

            for (String name : names) {
                try {
                    String uuid = plugin.players.getValueFromKey(name).toString();
                    if (!plugin.getSaveStatsRealTime()) {
                        if (plugin.statsForPlayers.containsKey(uuid)) {
                            if (plugin.statsForPlayers.get(uuid).containsKey(StatTypes.DEATH_LOCATIONS.id)) {
                                respondToDeathLocations(plugin.statsForPlayers.get(uuid).get(StatTypes.DEATH_LOCATIONS.id), uuid, sender, args);
                            } else {
                                File statFile = new File(plugin.getDataFolder(), "stats/" + uuid + "/" + 29);
                                HashMap<String, Integer> map = getMapFromFile(statFile);

                                respondToDeathLocations(map, uuid, sender, args);
                            }
                        } else {
                            File statFile = new File(plugin.getDataFolder(), "stats/" + uuid + "/" + 29);
                            HashMap<String, Integer> map = getMapFromFile(statFile);

                            respondToDeathLocations(map, uuid, sender, args);
                        }
                    } else {
                        File statFile = new File(plugin.getDataFolder(), "stats/" + uuid + "/" + 29);
                        HashMap<String, Integer> map = getMapFromFile(statFile);

                        respondToDeathLocations(map, uuid, sender, args);
                    }
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f - Death Locations: 0", args, sender, StatTypes.DEATH);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void respondToDeathLocations(HashMap<String, Integer> map, String name, CommandSender sender, String[] args) {
        if (map == null) {
            String message = "§c" + name + "§f - Death Locations: 0";
            respondToCommand(message, args, sender, null);
            return;
        }

        Iterator it = map.entrySet().iterator();

        String message = "§c" + name + "§f - Death Locations: ";
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();

            String worldName = (String) pairs.getKey();
            if (!worldName.equalsIgnoreCase("total")) {
                int deaths = (int) pairs.getValue();

                // TODO: implement world aliases
                message = message + worldName + ":§6" + df.format(deaths) + "§f ";
            }
        }
        respondToCommand(message, args, sender, null);
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return df.format(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Deaths";
    }
}
