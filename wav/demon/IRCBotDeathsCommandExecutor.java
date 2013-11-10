package wav.demon;

import jerklib.Session;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
                String name = sender.getName();
                try {
                    File deathList = new File("/opt/msm/servers/ocminecraft/deaths/" + name);
                    Scanner scanner = new Scanner(deathList);
                    String deaths = scanner.nextLine();
                    session.getChannel("#ocminecraft").say(name + " has died " + deaths + " times!");
                    scanner.close();
                } catch (FileNotFoundException e) {
                    session.getChannel("#ocminecraft").say(name + " has no deaths on record!");
                    return true;
                }
                return true;
            }
        }
        // otherwise, go through the array and print deaths for each player
        for (String name : args) {
            try {
                File deathList = new File("/opt/msm/servers/ocminecraft/deaths/" + name);
                Scanner scanner = new Scanner(deathList);
                String deaths = scanner.nextLine();
                plugin.getServer().broadcastMessage(name + " has died " + deaths + " times!");
                scanner.close();
            } catch (FileNotFoundException e) {
                plugin.getServer().broadcastMessage(name + " has no deaths on record!");
            }
        }
        return true;
    }
}
