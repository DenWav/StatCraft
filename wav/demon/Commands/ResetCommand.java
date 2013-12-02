package wav.demon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResetCommand implements CommandExecutor {

    private StatCraft plugin;

    public ResetCommand(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 3)
            return false;
        else if (strings[0].equalsIgnoreCase("force-all") && strings.length == 1) {
            if (commandSender.hasPermission("admin.*")) {
                File statsDir = new File(plugin.getDataFolder(), "stats");
                try {
                    deleteRecursive(statsDir);
                } catch (FileNotFoundException e) {
                    System.out.println("StatCraft: Fatal Error occurred while trying to delete stats.");
                    e.printStackTrace();
                }
                plugin.statsForPlayers = new HashMap<String, Map<Integer, Map<String, Integer>>>();
                for (Player player :commandSender.getServer().getOnlinePlayers()) {
                    plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, player.getName(),
                            (int) (System.currentTimeMillis() / 1000));
                }
                return true;
            }
            commandSender.sendMessage("You don't have permission to do that.");
            return false;
        } else if (strings[0].equalsIgnoreCase("force-all") && strings.length == 2) {
            if (commandSender.hasPermission("admin.*")) {
                plugin.saveStatFiles();
                String type = strings[1];

                for (StatTypes stat : StatTypes.values())
                    if (type.equalsIgnoreCase(stat.title))
                        type = stat.id + "";

                File statsDir = new File(plugin.getDataFolder(), "stats");
                try {
                    deleteType(statsDir, type);
                    plugin.reloadStatFiles();
                } catch (FileNotFoundException e) {
                    System.out.println("StatCraft: Fatal Error occurred while trying to delete stats.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("StatCraft: Fatal Error occurred while trying to reload stats.");
                    e.printStackTrace();
                }
                for (Player player : commandSender.getServer().getOnlinePlayers()) {
                    plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, player.getName(),
                            (int) (System.currentTimeMillis() / 1000));
                }
                return true;
            }
            commandSender.sendMessage("You don't have permission to do that.");
            return false;
        } else if (strings[0].equalsIgnoreCase("force") && strings.length == 2) {
            String name = strings[1];
            if (name.equalsIgnoreCase(commandSender.getName())) {
                File statsDir = new File(plugin.getDataFolder(), "stats/" + name);
                try {
                    deleteRecursive(statsDir);
                } catch (FileNotFoundException e) {
                    System.out.println("Fatal Error occurred while trying to delete " + name + "'s stats.");
                    e.printStackTrace();
                }
                if (plugin.statsForPlayers.containsKey(name))
                    plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

                plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, name,
                        (int) (System.currentTimeMillis() / 1000));
                return true;
            } else if (commandSender.hasPermission("admin.*")) {
                File statsDir = new File(plugin.getDataFolder(), "stats/" + name);
                try {
                    deleteRecursive(statsDir);
                } catch (FileNotFoundException e) {
                    System.out.println("Fatal Error occurred while trying to delete " + name + "'s stats.");
                    e.printStackTrace();
                }
                if (plugin.statsForPlayers.containsKey(name))
                    plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

                plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, name,
                        (int) (System.currentTimeMillis() / 1000));
                return true;
            } else {
                commandSender.sendMessage("You don't have permission to do that.");
            }
            return false;
        } else if (strings[0].equalsIgnoreCase("force") && strings.length == 3) {
            String name = strings[1];
            String typeString = strings[2];

            for (StatTypes stat : StatTypes.values())
                if (typeString.equalsIgnoreCase(stat.title))
                    typeString = stat.id + "";

            int type;
            try {
                type = Integer.parseInt(typeString);
            } catch (NumberFormatException e) {
                return false;
            }
            if (name.equalsIgnoreCase(commandSender.getName())) {
                File statsDir = new File(plugin.getDataFolder(), "stats/" + name);
                try {
                    deleteType(statsDir, typeString);
                } catch (FileNotFoundException e) {
                    System.out.println("Fatal Error occurred while trying to delete " + name + "'s stats.");
                    e.printStackTrace();
                }

                if (plugin.statsForPlayers.containsKey(name))
                    if (plugin.statsForPlayers.get(name).containsKey(type))
                        plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

                if (type == StatTypes.LAST_JOIN_TIME.id)
                    plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, name,
                            (int) (System.currentTimeMillis() / 1000));

                return true;
            } else if (commandSender.hasPermission("admin.*")) {
                File statsDir = new File(plugin.getDataFolder(), "stats/" + name);
                try {
                    deleteType(statsDir, typeString);
                } catch (FileNotFoundException e) {
                    System.out.println("Fatal Error occurred while trying to delete " + name + "'s stats.");
                    e.printStackTrace();
                }

                if (plugin.statsForPlayers.containsKey(name))
                    if (plugin.statsForPlayers.get(name).containsKey(type))
                        plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

                if (type == StatTypes.LAST_JOIN_TIME.id)
                    plugin.playtime.addStatToPlayer(StatTypes.LAST_JOIN_TIME.id, name,
                            (int) (System.currentTimeMillis() / 1000));

                return true;
            } else {
                commandSender.sendMessage("You don't have permission to do that.");
            }
            return false;
        } else {
            return false;
        }
    }

    private static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    private static boolean deleteType(File path, String type) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteType(f, type);
            }
        }
        if (path.getName().equalsIgnoreCase(type))
            return ret && path.delete();
        else
            return ret;
    }
}
