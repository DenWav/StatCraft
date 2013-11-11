package wav.demon;

import com.google.gson.Gson;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class DeathsListener implements Listener{

    private StatCraft plugin;

    public DeathsListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        final String deathMessage = event.getDeathMessage();
        final String name = event.getEntity().getName();
        addDeathType(name, deathMessage);
    }

    private void addDeathType(String name, String deathMessage) {

        Map<String, Map<String, Map<String, Integer>>> statsForPlayers = plugin.getMap();
        if (statsForPlayers.get(name) == null) {
            statsForPlayers.put(name, new HashMap<String, Map<String, Integer>>());
        }
        if (statsForPlayers.get(name).get("death") == null) {
            statsForPlayers.get(name).put("death", new HashMap<String, Integer>());
        }
        if (statsForPlayers.get(name).get("death").get(deathMessage) == null) {
            statsForPlayers.get(name).get("death").put(deathMessage, 1);
            statsForPlayers.get(name).get("death").put("total", 1);
        } else {
            statsForPlayers.get(name).get("death").put(deathMessage, statsForPlayers.get(name).get("death").get(deathMessage) + 1);
            statsForPlayers.get(name).get("death").put("total", statsForPlayers.get(name).get("death").get("total") + 1);
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
