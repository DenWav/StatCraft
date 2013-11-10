package wav.demon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class IRCBot extends JavaPlugin {

    IRC irc = new IRC(this);
    private Map<String, Map<String, Map<String, Integer>>> statsForPlayers;

    @Override
    public void onEnable() {
        irc.enableIRC();
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, Map<String, Integer>>>>(){}.getType();
        // need to do a few things
        // 1. the input string must be the full JSON, so read the JSON file into a String
        // 2. use the FileNotFoundException to create a clean statsForPlayers if the stats file doesn't exist
        statsForPlayers = gson.fromJson("/opt/msm/servers/ocminecraft/stats.txt", type);
        getCommand("list").setExecutor(new IRCBotListCommandExecutor(this, irc.getSession()));
        getCommand("deaths").setExecutor(new IRCBotDeathsCommandExecutor(this, irc.getSession()));
        getCommand("kill").setExecutor(new IRCBotKillCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathsListener(this), this);
    }

    @Override
    public void onDisable() {
        irc.disableIRC();
    }

    public Map<String, Map<String, Map<String, Integer>>> getMap() {
        return statsForPlayers;
    }
}
