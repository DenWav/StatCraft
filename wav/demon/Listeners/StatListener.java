package wav.demon.Listeners;

import com.google.gson.Gson;
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

    private synchronized void addStatToPlayer(int type, String name, String message) {

        // check if they have any stats yet, if not, make one
        if (plugin.statsForPlayers.get(name) == null)
            plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

        // check if they have any stats for this event yet, if not, make one
        if (plugin.statsForPlayers.get(name).get(type) == null)
            plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

        // check if they have this particular event yet, if not, set to one. If so, increment it
        if (plugin.statsForPlayers.get(name).get(type).get(message) == null)
            plugin.statsForPlayers.get(name).get(type).put(message, 1);
        else
            plugin.statsForPlayers.get(name).get(type).put(message, plugin.statsForPlayers.get(name).get(type).get(message) + 1);

        // check to see if they have a total yet. If so, increment it; if not, set to 1
        if (plugin.statsForPlayers.get(name).get(type).get("total") == null)
            plugin.statsForPlayers.get(name).get(type).put("total", 1);
        else
            plugin.statsForPlayers.get(name).get(type).put("total", plugin.statsForPlayers.get(name).get(type).get("total") + 1);

        try {
            Gson gson = new Gson();
            String json = gson.toJson(plugin.statsForPlayers);
            PrintWriter out = new PrintWriter("/opt/msm/servers/ocminecraft/stats.txt");
            out.println(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void addStat(final int type, final String name, final String message) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                addStatToPlayer(type, name, message);
            }
        });
    }
}