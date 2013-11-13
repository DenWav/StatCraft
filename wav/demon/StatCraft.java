package wav.demon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import wav.demon.Commands.*;
import wav.demon.Commands.KillCommand;
import wav.demon.Listeners.BlockListener;
import wav.demon.Listeners.DeathListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class StatCraft extends JavaPlugin {

    public volatile Map<String, Map<Integer, Map<String, Integer>>> statsForPlayers;
    private TimedActivities timedActivities;
    // TODO: enable config support
    private boolean enabled = true;

    @Override
    public void onEnable() {
        // See if the config file exists
        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists())
            saveDefaultConfig();
        else
            getConfig();

        // make sure the stats directory exists
        File stat = new File(getDataFolder(), "stats");
        if (stat.exists() && stat.isDirectory()) {
            // it exists and it is a directory, so load up the old stats
            // try to reload the stats, if possible
            try {
                if (reloadStatFiles()) {
                    // yay, it worked
                    System.out.println("StatCraft: Old stats loaded successfully.");
                    Gson gson = new Gson();
                    System.out.println("StatCraft: " + gson.toJson(statsForPlayers));
                } else {
                    // something isn't quite right, so start from scratch
                    statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
                }
            } catch (IOException e) {
                System.out.println("StatCraft: Something when wrong when trying to read the old stats. Could not initialize.");
                e.printStackTrace();
            }
        } else if (!stat.exists()) {
            // the directory doesn't exist, so make a new one and create a new stat HashMap
            stat.mkdir();
            statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
        } else if (!stat.isDirectory()) {
            // the file exists, but it's not a directory, so warn the user
            System.out.println("StatCraft: stats file in the plugin data folder is not a directory, cannot initialize!");
            enabled = false;
        }
        // load up the listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        // load up commands
        getCommand("list").setExecutor(new ListCommand());
        getCommand("deaths").setExecutor(new DeathListener(this));
        getCommand("blocks").setExecutor(new BlockListener(this));
        getCommand("kill").setExecutor(new KillCommand(this));
        getCommand("resetstats").setExecutor(new ResetCommand(this));
        getCommand("printdata").setExecutor(new PrintData(this));
        getCommand("updatetotals").setExecutor(new UpdateTotals(this));

        timedActivities = new TimedActivities(this);

        System.out.println("Successfully started totals updating: " + timedActivities.startTotalsUpdateing(10));

    }

    @Override
    public void onDisable() {
        // set the first iterator
        Iterator baseIt = statsForPlayers.entrySet().iterator();
        while (baseIt.hasNext()) {
            // grab the first pair, then the name and the second map
            Map.Entry pairs = (Map.Entry) baseIt.next();
            String name = (String) pairs.getKey();
            if (!name.equalsIgnoreCase("total")) {
                Map<Integer, Map<String, Integer>> secondaryMap = (Map<Integer, Map<String, Integer>>) pairs.getValue();
                // set the second iterator off of the second map
                Iterator secondaryIt = secondaryMap.entrySet().iterator();
                while (secondaryIt.hasNext()) {
                    // grab the second pair and the type
                    Map.Entry secondPairs = (Map.Entry) secondaryIt.next();
                    int type = (Integer) secondPairs.getKey();
                    try {
                        // set gson and grab the json text out of the second map's "vale" area
                        Gson gson = new Gson();
                        String json = gson.toJson(secondPairs.getValue());

                        // make sure the directory exists for us to write to
                        File outputDir = new File(this.getDataFolder(), "stats/" + name);
                        PrintWriter out;
                        if (outputDir.exists()) {
                            out = new PrintWriter(outputDir.toString() + "/" + type);
                        } else {
                            outputDir.mkdirs();
                            out = new PrintWriter(outputDir.toString() + "/" + type);
                        }

                        // write the json to the file
                        out.println(json);
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        System.out.println("Successfully stopped totals updating: " + timedActivities.stopTotalsUpdating());
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    private boolean reloadStatFiles() throws IOException {
        statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
        if (getDataFolder().exists()) {
            // check the root stats directory
            File statsDir = new File(getDataFolder(), "stats");
            // make sure the directory exists
            if (statsDir.exists()) {
                // check the individual player directories
                for (File player : statsDir.listFiles()) {
                    // get the player's name
                    String name = player.getName();
                    if (!name.equalsIgnoreCase("totals")) {
                        System.out.println("StatCraft: " + name + " found at: " + player.getPath());
                        // put player's name into map, now we need to get the stats themselves
                        statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());
                        for (File type : player.listFiles()) {
                            // grab the statType
                            String statType = type.getName();
                            // set Token type and gson
                            Gson gson = new Gson();
                            Type tokenType = new TypeToken<Map<String, Integer>>(){}.getType();
                            // insert the stats into the map
                            statsForPlayers.get(name).put(Integer.parseInt(statType), (Map<String, Integer>)
                                    gson.fromJson(readFile(type.getPath(), StandardCharsets.UTF_8), tokenType));
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public TimedActivities getTimedActivities() {
        return timedActivities;
    }
}