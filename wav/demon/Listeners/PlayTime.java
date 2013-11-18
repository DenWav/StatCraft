package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class PlayTime extends StatListener implements CommandExecutor {

    StatCraft plugin;

    public PlayTime(StatCraft plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) System.currentTimeMillis() / 1000;
        addStat(StatTypes.LAST_JOIN_TIME, name, currentTime);
    }

    @SuppressWarnings({"unused", "deprecation"})
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) System.currentTimeMillis() / 1000;
        addStat(StatTypes.LAST_LEAVE_TIME, name, currentTime);

        addStat(StatTypes.PLAY_TIME, name, calculateTimeInterval(name, currentTime));
    }

    /** Only call this method on PlayerQuitEvent! */
    private int calculateTimeInterval(String name, final int leaveTime) {
        final int currentPlayTime = getStat(name, StatTypes.PLAY_TIME);
        final int joinTime = getStat(name, StatTypes.LAST_JOIN_TIME);

        int playTime = (leaveTime - joinTime) + currentPlayTime;
        return playTime;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("playtime")) {
            // list the playtime for a player
            String[] names = getPlayers(sender, args);
            if (names == null)
                return false;

            // otherwise, go through the array and print playtime for each player
            int playTime;
            for (String name : names) {
                if (sender.getServer().getPlayer(name) != null) {
                    final int startTime = (int) System.currentTimeMillis() / 1000;
                    final int currentTimePlayed = startTime - getStat(name, StatTypes.LAST_JOIN_TIME);
                    playTime = currentTimePlayed + getStat(name, StatTypes.PLAY_TIME);
                } else {
                    playTime = getStat(name, StatTypes.PLAY_TIME);
                }

                // Figure out the playtime in a human readable format
                final int seconds = playTime / 1000;

                final int secondsInMinute = 60;
                final int secondsInHour = 60 * secondsInMinute;
                final int secondsInDay = 24 * secondsInHour;
                final int secondsInWeek = 7 * secondsInDay;

                final int weeks = seconds / secondsInWeek;

                final int daySeconds = seconds % secondsInWeek;
                final int days = daySeconds / secondsInDay;

                final int hourSeconds = daySeconds % secondsInDay;
                final int hours = hourSeconds / secondsInHour;

                final int minuteSeconds = hourSeconds % secondsInHour;
                final int minutes = minuteSeconds / secondsInMinute;

                final int remainingSeconds = minuteSeconds % secondsInMinute;

                // Make some strings
                String weekString;
                String dayString;
                String hourString;
                String minuteString;
                String secondString;

                // Use correct grammar, and don't use it if it's zero
                if (weeks == 1)
                    weekString = weeks + " week";
                else if (weeks == 0)
                    weekString = "";
                else
                    weekString = weeks + " weeks";

                if (days == 1)
                    dayString = days + " day";
                else if (days == 0)
                    dayString = "";
                else
                    dayString = days + " days";

                if (hours == 1)
                    hourString = hours + " hour";
                else if (hours == 0)
                    hourString = "";
                else
                    hourString = hours + " hours";

                if (minutes == 1)
                    minuteString = minutes + " minute";
                else if (minutes == 0)
                    minuteString = "";
                else
                    minuteString = minutes + " minutes";

                if (remainingSeconds == 1)
                    secondString = remainingSeconds + " second";
                else if (remainingSeconds == 0)
                    secondString = "";
                else
                    secondString = remainingSeconds + " seconds";

                // print out the results
                if (weekString.equals("") && dayString.equals("") && hourString.equals("") &&
                        minuteString.equals("") && secondString.equals("")) {
                    sender.getServer().broadcastMessage(name + " doesn't have any logged playtime yet.");
                } else {
                    ArrayList<String> results = new ArrayList<String>();
                    results.add(weekString);
                    results.add(dayString);
                    results.add(hourString);
                    results.add(minuteString);
                    results.add(secondString);

                    for (int x = results.size() - 1; x >= 0; x--) {
                        if (results.get(x).equals("")) {
                            results.remove(x);
                        }
                    }

                    String finalResult = "";
                    for (int x = 0; x < results.size(); x++) {
                        if (x == results.size() - 1) {
                            if (x == 0)
                                finalResult = results.get(x) + ".";
                            else
                                finalResult = finalResult + ", " + results.get(x) + ".";
                        } else {
                            if (x == 0)
                                finalResult = results.get(x);
                            else
                                finalResult = finalResult + ", " + results.get(x);
                        }
                    }
                    sender.getServer().broadcastMessage(name + " - Playtime: " + finalResult);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("lastseen")) {
            // list the time we last saw a player
            if (!(sender instanceof Player)) {
                // if this is run from the console, then a player name must be provided
                if (args.length == 0) {
                    // tell them to provide only one name and print usage
                    sender.sendMessage("You must name someone to list play times for from the console!");
                    return false;
                }
            }

            String[] names;
            if (args.length == 0)
                names = new String[] {sender.getName()};
            else
                names = args;

            for (String name : names) {
                if (sender.getServer().getPlayer(name) != null) {
                    sender.getServer().broadcastMessage(name + " is online now!");
                } else {
                    Date date = new Date(getStat(name, StatTypes.LAST_LEAVE_TIME));
                    SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("CST"));
                    sender.getServer().broadcastMessage(name + " - Last Seen: " + format.format(date));
                }
            }
            return true;
        }
        return false;
    }
}
