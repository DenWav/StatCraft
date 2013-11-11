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

    protected void addStat(int type, String name, String message) {

        Map<String, Map<Integer, Map<String, Integer>>> statsForPlayers = plugin.getMap();
        if (statsForPlayers.get(name) == null) {
            statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());
        }
        if (statsForPlayers.get(name).get(type) == null) {
            statsForPlayers.get(name).put(type, new HashMap<String, Integer>());
        }
        if (statsForPlayers.get(name).get(type).get(message) == null) {
            statsForPlayers.get(name).get(type).put(message, 1);
            statsForPlayers.get(name).get(type).put("total", 1);
        } else {
            statsForPlayers.get(name).get(type).put(message, statsForPlayers.get(name).get(1).get(message) + 1);
            statsForPlayers.get(name).get(type).put("total", statsForPlayers.get(name).get(1).get("total") + 1);
        }
        try {
            Gson gson = new Gson();
            String json = gson.toJson(statsForPlayers);
            PrintWriter out = new PrintWriter("/opt/msm/servers/ocminecraft/stats.txt");
            out.println(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
