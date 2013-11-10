package wav.demon;

import jerklib.Session;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;

public class IRCBotDeathsCommandExecutor implements CommandExecutor {

    private IRCBot plugin;
    private Session session;

    public IRCBotDeathsCommandExecutor(IRCBot plugin, Session session) {
        this.plugin = plugin;
        this.session = session;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // list the number of recorded deaths for a player
        // first, figure out which player to list deaths for
        if (!(sender instanceof Player)) {
            // if this is run from the console, then a player name must be provided
            if (args.length == 0) {
                // tell them to provide only one name and print usage
                sender.sendMessage("You must name someone to list deaths for from the console!");
                return false;
            }
        } else {
            // if no arguments were given from a player, simply use him as the name
            if (args.length == 0) {
                int deaths = 0;
                String name = sender.getName();
                Iterator it = plugin.getMap().get(name).get("death").entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    deaths += (Integer) pairs.getValue();
                    //it.remove();
                }
                if (deaths == 1) {
                    session.getChannel("#ocminecraft").say(name + " has died " + deaths + " time.");
                } else {
                    session.getChannel("#ocminecraft").say(name + " has died " + deaths + " times.");
                }
                return true;
            }
        }
        // otherwise, go through the array and print deaths for each player
        int deaths = 0;
        for (String name : args) {
            Iterator it = plugin.getMap().get(name).get("deaths").entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                deaths += (Integer) pairs.getValue();
                //it.remove();
            }
            if (deaths == 1) {
                session.getChannel("#ocminceraft").say(name + " has died " + deaths + " time.");
            } else {
                session.getChannel("#ocminecraft").say(name + " has died " + deaths + " times.");
            }
        }
        return true;
    }
}
