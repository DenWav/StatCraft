package wav.demon.Listeners;

import com.google.common.collect.Ordering;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayTime extends StatListener {

    public PlayTime(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {

        if (plugin.players.containsValue(event.getPlayer().getUniqueId())) {
            if (!plugin.players.containsKey(event.getPlayer().getName())) {
                // this player must have gotten a name change, we have the UUID on file, but not the username
                plugin.players.removeValue(event.getPlayer().getUniqueId());
                plugin.players.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
            } else if (plugin.players.getKeyFromValue(event.getPlayer().getUniqueId()) == null) {
                // null check
                plugin.players.removeValue(event.getPlayer().getUniqueId());
                plugin.players.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
            } else //noinspection ConstantConditions
                if (!plugin.players.getKeyFromValue(event.getPlayer().getUniqueId()).equals(event.getPlayer().getName())) {
                // This is a bit of a problem. It appears that someone already on the server changed their name, and then
                // someone else already on the server also changed their name before the other play could reconnect, so
                // the old pairing is wrong. We know the player with the conflicting nickname isn't online, or it would
                // have been resolved by now, so we will simply change it to null.
                UUID uuid = plugin.players.getValueFromKey(event.getPlayer().getName());
                plugin.players.removeValue(uuid);
                plugin.players.put(null, uuid);
                plugin.players.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
            }
        } else if (plugin.players.containsKey(event.getPlayer().getName())) {
            // The mapping is wrong from two people doing name changes mentioned earlier, so set the current to null
            // so it will be fixed when the player later connects
            UUID uuid = plugin.players.getValueFromKey(event.getPlayer().getName());
            plugin.players.removeValue(uuid);
            plugin.players.put(null, uuid);
            plugin.players.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        } else {
            plugin.players.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        }

        plugin.writePlayersFile();

        final String uuid = event.getPlayer().getUniqueId().toString();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (plugin.getLast_join_time())
            addStat(StatTypes.LAST_JOIN_TIME.id, uuid, currentTime);
        if (plugin.getJoins())
            addStat(StatTypes.JOINS.id, uuid, getStat(uuid, StatTypes.JOINS.id) + 1);

        plugin.highestLevel.updateHighestLevel(event.getPlayer());
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (plugin.getLast_leave_time())
            addStat(StatTypes.LAST_LEAVE_TIME.id, uuid, currentTime);

        if (plugin.getPlay_time())
            addStat(StatTypes.PLAY_TIME.id, uuid, calculateTimeInterval(uuid, currentTime));
    }

    // NOTE: Only call this method on PlayerQuitEvent!
    private int calculateTimeInterval(String name, final int leaveTime) {
        final int currentPlayTime = getStat(name, StatTypes.PLAY_TIME.id);
        final int joinTime = getStat(name, StatTypes.LAST_JOIN_TIME.id);

        return (leaveTime - joinTime) + currentPlayTime;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("playtime")) {
            // list the playtime for a player
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            // otherwise, go through the array and print playtime for each player
            int playTime;
            for (String name : names) {
                try {
                    if (sender.getServer().getPlayer(plugin.players.getValueFromKey(name)) != null) {
                        final int startTime = (int) (System.currentTimeMillis() / 1000);
                        final int currentTimePlayed = startTime - getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.LAST_JOIN_TIME.id);
                        playTime = currentTimePlayed + getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.PLAY_TIME.id);
                    } else {
                        playTime = getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.PLAY_TIME.id);
                    }

                    String message = transformTime(playTime);

                    if (message.equalsIgnoreCase("")) {
                        message = "§c" + name + "§f doesn't have any logged playtime yet.";
                        respondToCommand(message, args, sender, StatTypes.PLAY_TIME);
                    } else {
                        int thisSession = (int) (System.currentTimeMillis() / 1000) - getStat(name, StatTypes.LAST_JOIN_TIME.id);
                        String thisSessionText = transformTime(thisSession);

                        message = "§c" + name + "§f - Playtime: " + message + " | This session: " + thisSessionText;
                        respondToCommand(message, args, sender, StatTypes.PLAY_TIME);
                    }
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f doesn't have any logged playtime yet.", args, sender, StatTypes.PLAY_TIME);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("lastseen")) {
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String arg : args) {
                if (arg.startsWith("-top")) {
                    sender.sendMessage("\"-top\" is not allowed with the lastseen command.");

                    return true;
                }
            }

            for (String name : names) {
                try {
                    if (sender.getServer().getPlayer(plugin.players.getValueFromKey(name)) != null) {
                        sender.getServer().broadcastMessage(name + " is online now!");
                    } else {
                        long dateTime = (long) getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.LAST_LEAVE_TIME.id) * 1000;
                        if (dateTime == 0) {
                            String message = "§c" + name + "§f hasn't been seen on the server yet.";
                            respondToCommand(message, args, sender, null);
                        } else {
                            Date date = new Date(dateTime);
                            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                            format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                            String message = "§c" + name + "§f - Last Seen: " + format.format(date);
                            respondToCommand(message, args, sender, null);
                        }
                    }
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f hasn't been seen on the server yet.", args, sender, null);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("joins")) {
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                try {
                    String joins = df.format(getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.JOINS.id));
                    String message = "§c" + name + "§f - Joins: " + joins;
                    respondToCommand(message, args, sender, StatTypes.JOINS);
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f - Joins: 0", args, sender, StatTypes.JOINS);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        if (type == StatTypes.JOINS)
            return df.format(value);
        else
            return transformTime(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        if (type == StatTypes.JOINS)
            return "Joins";
        else
            return "Play Time";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void respondToCommand(String message, String[] args, CommandSender sender, StatTypes type) {
        boolean publicCmd = false;
        boolean top = false;
        int topNumber = 0;

        for (String arg : args) {
            if (arg.equals("-all"))
                publicCmd = true;

            if (arg.startsWith("-top")) {
                top = true;
                try {
                    topNumber = Integer.valueOf(arg.replace("-top", ""));
                } catch (NumberFormatException e) {
                    sender.sendMessage("Not a valid \"-top\" value. Please use \"-top#\" with # being an integer.");
                    return;
                }
            }
        }

        if (!top) {
            if (publicCmd)
                sender.getServer().broadcastMessage("§3@" + sender.getName() + "§f: " + message);
            else
                sender.sendMessage(message);
        } else {
            Map<String, Integer> sortableMap = Collections.synchronizedMap(new ValueComparableMap<String, Integer>(Ordering.from(Collections.reverseOrder())));

            File statsDir = new File(plugin.getDataFolder(), "stats");
            File[] files = statsDir.listFiles();

            if (files != null) {
                for (File name : files) {
                    if (!name.getName().equalsIgnoreCase("total")) {
                        File typeFile = new File(name, type.id + "");
                        Map<String, Integer> typeMap = getMapFromFile(typeFile);
                        if (typeMap != null) {
                            Integer total = typeMap.get("total");
                            if (total != null) {
                                sortableMap.put(name.getName(), total);
                            }
                        }
                    }
                }
            }


            for (Player player : plugin.getServer().getOnlinePlayers()) {
                int playTime;
                final int startTime = (int) (System.currentTimeMillis() / 1000);
                final int currentTimePlayed = startTime - getStat(player.getName(), StatTypes.LAST_JOIN_TIME.id);
                playTime = currentTimePlayed + getStat(player.getName(), StatTypes.PLAY_TIME.id);

                sortableMap.put(player.getName(), playTime);
            }

            String output = typeLabel(type) + " - ";
            Iterator iterator = sortableMap.entrySet().iterator();
            for (int i = 1; i <= topNumber; i++) {
                if (iterator.hasNext()) {
                    Map.Entry<String, Integer> sortedMapEntry = (Map.Entry<String, Integer>) iterator.next();

                    String name = sortedMapEntry.getKey();
                    Integer value = sortedMapEntry.getValue();

                    output = output + "§6" + i + ". §c" + name + "§f: " + typeFormat(value, type) + " ";
                } else {
                    break;
                }
            }

            if (publicCmd)
                sender.getServer().broadcastMessage("§3@" + sender.getName() + "§f: " + output);
            else
                sender.sendMessage(output);

        }
    }
}
