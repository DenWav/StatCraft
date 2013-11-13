package wav.demon.Commands;

import com.google.gson.Gson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import wav.demon.StatCraft;

public class PrintData implements CommandExecutor {

    StatCraft plugin;

    public PrintData(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Gson gson = new Gson();
        System.out.println(gson.toJson(plugin.statsForPlayers));
        return true;
    }
}
