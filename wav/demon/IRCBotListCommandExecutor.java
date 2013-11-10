package wav.demon;

import jerklib.Session;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IRCBotListCommandExecutor implements CommandExecutor{

    private IRCBot plugin;
    private Session session;

    public IRCBotListCommandExecutor(IRCBot plugin, Session session) {
        this.plugin = plugin;
        this.session = session;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list players on the server (this is more for IRC)
        String playerList = "";
        for (Player player : plugin.getServer().getOnlinePlayers())
            playerList = playerList + player.getName() + " ";

        if (playerList.equalsIgnoreCase("")) {
            session.getChannel("#ocminecraft").say("There aren't currently any players online!");
        } else {
            session.getChannel("#ocminecraft").say(playerList);
        }
        return true;
    }
}
