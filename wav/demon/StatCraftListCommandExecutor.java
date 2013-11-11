package wav.demon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCraftListCommandExecutor implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String playerList = "";
        for (Player player : sender.getServer().getOnlinePlayers())
            playerList = playerList + player.getName() + " ";

        if (playerList.equalsIgnoreCase("")) {
            sender.getServer().broadcastMessage("There aren't currently any players online!");
        } else {
            sender.getServer().broadcastMessage(playerList);
        }
        return true;
    }
}
