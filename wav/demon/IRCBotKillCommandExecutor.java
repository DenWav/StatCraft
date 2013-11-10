package wav.demon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IRCBotKillCommandExecutor implements CommandExecutor {

    private IRCBot plugin;

    public IRCBotKillCommandExecutor(IRCBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // first check the arguments to make sure they are well formed
        // check to make sure there is not more than 2 arguments
        if (args.length > 2) {
            sender.sendMessage("This command can only be run with one argument.");
            return false;
        // check to make sure there is at least one argument
        } else if (args.length < 1) {
            sender.sendMessage("You must specify a player to kill.");
            return false;
        }
        String type;
        // check the number of arguments
        if (args.length == 2) {
            // there are two arguments, so set the type as the second argument
            type = args[1];
            // check to see if the person who sent the command is a player or not
            if (sender instanceof Player) {
                // the person who sent the command is a player
                Player player = (Player) sender;
                // set the target as the first argument
                Player target = (plugin.getServer().getPlayer(args[0]));
                if (target == null) {
                    // target isn't on the server right now, return false
                    sender.sendMessage(args[0] + " is not online!");
                    return false;
                } else {
                    // check what type of kill the sender wants
                    if (type.equalsIgnoreCase("explode")) {
                        // make sure the player has permission to issue this command
                        if (player.hasPermission("killer.explode")) {
                            // he does, so make a fake explosion and kill the target
                            target.getWorld().createExplosion(target.getLocation(), 0);
                            target.setHealth(0);
                            return true;
                        } else {
                            // he doesn't, return false
                            sender.sendMessage("You don't have permission to do that!");
                            return false;
                        }
                    } else if (type.equalsIgnoreCase("slap")) {
                        // make sure the player has permission to issue this command
                        if (player.hasPermission("killer.slap")) {
                            // he does, so subtract one from teh target's health
                            target.setHealth(target.getHealth() - 1);
                            return true;
                        } else {
                            // he doesn't return false
                            sender.sendMessage("You don't have permission to do that!");
                            return false;
                        }
                    } else {
                        // the type the sender specified was incorrect, return false
                        sender.sendMessage("You must specify between \"slap\" and \"explode\"");
                        return false;
                    }
                }
            } else {
                // the sender is not a player, so we know he has permission to run the command
                Player target = (plugin.getServer().getPlayer(args[0]));
                // check to see if the target is online
                if (target == null) {
                    // the target isn't online, so return false
                    sender.sendMessage(args[0] + " is not online!");
                    return false;
                } else {
                    // check the type of kill the sender wants
                    if (type.equalsIgnoreCase("explode")) {
                        // we know the sender has permission, so make a fake explosion and return true
                        target.getWorld().createExplosion(target.getLocation(), 0);
                        target.setHealth(0);
                        return true;
                    } else if (type.equalsIgnoreCase("slap")) {
                        // we know the sender has permission, so subtract one from the player's health and return true
                        target.setHealth(target.getHealth() - 1);
                        return true;
                    } else {
                        // the type the sender specified was incorrect, return false
                        sender.sendMessage("You must specify between \"slap\" and \"explode\"");
                        return false;
                    }
                }
            }
        } else {
            // there was no type specified, so just do a regular kill
            if (sender instanceof Player) {
                // the sender is a player
                Player player = (Player) sender;
                // check to ensure the target is logged on
                Player target = (plugin.getServer().getPlayer(args[0]));
                if (target == null) {
                    // the target is not logged on, so return false
                    sender.sendMessage(args[0] + " is not online!");
                    return false;
                } else {
                    // check to make sure the player has permission to run this command
                    if (player.hasPermission("killer.kill")) {
                        // the player does have permission, so kill the target and return true
                        target.setHealth(0);
                        return true;
                    } else {
                        // the player does not have permission, so return false
                        sender.sendMessage("You don't have permission to do that!");
                        return false;
                    }
                }
            } else {
                // the command was sent from the console, so we know the sender has permission
                Player target = (plugin.getServer().getPlayer(args[0]));
                // check to make sure the target is logged on
                if (target == null) {
                    // the target is not logged on, return false
                    sender.sendMessage(args[0] + " is not online!");
                    return false;
                } else {
                    // the target is logged on, so kill him and return true
                    target.setHealth(0);
                    return true;
                }
            }
        }
    }
}
