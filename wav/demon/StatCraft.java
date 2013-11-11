package wav.demon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import wav.demon.Listeners.BlockListener;
import wav.demon.Listeners.DeathListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class StatCraft extends JavaPlugin {

    private Map<String, Map<Integer, Map<String, Integer>>> statsForPlayers;

    @Override
    public void onEnable() {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<Integer, Map<String, Integer>>>>(){}.getType();
        try {
            statsForPlayers = gson.fromJson(readFile("/opt/msm/servers/ocminecraft/stats.txt", StandardCharsets.UTF_8), type);
        } catch (IOException e) {
            statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
        }
        getCommand("list").setExecutor(new StatCraftListCommandExecutor());
        getCommand("deaths").setExecutor(new StatCraftDeathsCommandExecutor(this));
        getCommand("kill").setExecutor(new StatCraftKillCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    public Map<String, Map<Integer, Map<String, Integer>>> getMap() {
        return statsForPlayers;
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
}