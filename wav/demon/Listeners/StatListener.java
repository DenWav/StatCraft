package wav.demon.Listeners;

import com.google.gson.Gson;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import wav.demon.StatCraft;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StatListener implements Listener {

    private StatCraft plugin;

    public StatListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    private synchronized void incrementStatToPlayer(int type, String name, String message) {

        // check if they have any stats yet, if not, make one
        if (!plugin.statsForPlayers.containsKey(name))
            plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

        // check if they have any stats for this event yet, if not, make one
        if (!plugin.statsForPlayers.get(name).containsKey(type))
            plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

        // check if they have this particular event yet, if not, set to one. If so, increment it
        if (!plugin.statsForPlayers.get(name).get(type).containsKey(message))
            plugin.statsForPlayers.get(name).get(type).put(message, 1);
        else
            plugin.statsForPlayers.get(name).get(type).put(message, plugin.statsForPlayers.get(name).get(type).get(message) + 1);

        // check to see if they have a total yet. If so, increment it; if not, set to 1
        if (!plugin.statsForPlayers.get(name).get(type).containsKey("total"))
            plugin.statsForPlayers.get(name).get(type).put("total", 1);
        else
            plugin.statsForPlayers.get(name).get(type).put("total", plugin.statsForPlayers.get(name).get(type).get("total") + 1);

        try {
            // declare the gson for writing the json
            Gson gson = new Gson();
            String json = gson.toJson(plugin.statsForPlayers.get(name).get(type));

            // ensure the output directory exists
            File outputDir = new File(plugin.getDataFolder(), "stats/" + name);

            // create the PrintWriter objects for writing the files
            PrintWriter out;

            // check if the directory exists, if not, create it
            if (!outputDir.exists())
                outputDir.mkdirs();

            // set the PrintWriter to the file we are going to write to
            out = new PrintWriter(outputDir.toString() + "/" + type);

            // write the json to the file
            out.println(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private synchronized void addStatToPlayer(int type, String name, int data) {

        // check if they have any stats yet, if not, make one
        if (!plugin.statsForPlayers.containsKey(name))
            plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

        // check if they have any stats for this event yet, if not, make one
        if (!plugin.statsForPlayers.get(name).containsKey(type))
            plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

        // add the stat to the total
        plugin.statsForPlayers.get(name).get(type).put("total", data);

        try {
            // declare the gson for writing the json
            Gson gson = new Gson();
            String json = gson.toJson(plugin.statsForPlayers.get(name).get(type));

            // ensure the output directory exists
            File outputDir = new File(plugin.getDataFolder(), "stats/" + name);

            // create the PrintWriter objects for writing the files
            PrintWriter out;

            // check if the directory exists, if not, create it
            if (!outputDir.exists())
                outputDir.mkdirs();

            // set the PrintWriter to the file we are going to write to
            out = new PrintWriter(outputDir.toString() + "/" + type);

            // write the json to the file
            out.println(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void incrementStat(final int type, final String name, final String message) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                incrementStatToPlayer(type, name, message);
            }
        });
    }

    protected void addStat(final int type, final String name, final int data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                addStatToPlayer(type, name, data);
            }
        });
    }

    protected int  getStat(String name, int type) {
        int stat;
        if (plugin.statsForPlayers.containsKey(name))
            if (plugin.statsForPlayers.get(name).containsKey(type))
                if (plugin.statsForPlayers.get(name).get(type).containsKey("total"))
                    stat = plugin.statsForPlayers.get(name).get(type).get("total");
                else
                    stat = 0;
            else
                stat = 0;
        else
            stat = 0;

        return stat;
    }

    protected String[] getPlayers(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // if this is run from the console, then a player name must be provided
            if (args.length == 0) {
                // tell them to provide only one name and print usage
                sender.sendMessage("You must name someone from the console!");
                return null;
            }
        }

        String[] names;
        if (args.length == 0)
            names = new String[] {sender.getName()};
        else
            names = args;

        return names;
    }
}