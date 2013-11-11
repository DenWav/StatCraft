package wav.demon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import wav.demon.Listeners.BlockListener;
import wav.demon.Listeners.DeathListener;

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
import java.util.Map;

public final class StatCraft extends JavaPlugin {

    public volatile Map<String, Map<Integer, Map<String, Integer>>> statsForPlayers;
    final private String statFile = "/opt/msm/servers/ocminecraft/stats.txt";

    @Override
    public void onEnable() {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<Integer, Map<String, Integer>>>>(){}.getType();
        try {
            statsForPlayers = gson.fromJson(readFile(statFile, StandardCharsets.UTF_8), type);
            System.out.println("StatCraft: " + statFile + " loaded successfully.");
        } catch (IOException e) {
            statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
        }
        getCommand("list").setExecutor(new StatCraftListCommandExecutor());
        getCommand("deaths").setExecutor(new StatCraftDeathsCommandExecutor(this));
        getCommand("kill").setExecutor(new StatCraftKillCommandExecutor(this));
        getCommand("resetstats").setExecutor(new StatCraftResetCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
    }

    @Override
    public void onDisable() {
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

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
}