package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerList = "";
        for (Player player : sender.getServer().getOnlinePlayers())
            if (playerList.length() == 0)
                playerList = player.getName();
            else
                playerList = playerList + ", " + player.getName();

        if (playerList.equalsIgnoreCase("")) {
            sender.getServer().broadcastMessage("There aren't any players online!");
        } else {
            sender.getServer().broadcastMessage(playerList);
        }
        return true;
    }
}
