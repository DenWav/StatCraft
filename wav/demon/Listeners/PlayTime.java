package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

    public PlayTime(StatCraft plugin) {
        super(plugin);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
            final String name = event.getPlayer().getName();
            final int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (plugin.getLast_join_time())
            addStat(StatTypes.LAST_JOIN_TIME.id, name, currentTime);
        if (plugin.getJoins())
            addStat(StatTypes.JOINS.id, name, getStat(name, StatTypes.JOINS.id) + 1);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (plugin.getLast_leave_time())
            addStat(StatTypes.LAST_LEAVE_TIME.id, name, currentTime);

        if (plugin.getPlay_time())
            addStat(StatTypes.PLAY_TIME.id, name, calculateTimeInterval(name, currentTime));
    }

    // NOTE: Only call this method on PlayerQuitEvent!
    private int calculateTimeInterval(String name, final int leaveTime) {
        final int currentPlayTime = getStat(name, StatTypes.PLAY_TIME.id);
        final int joinTime = getStat(name, StatTypes.LAST_JOIN_TIME.id);

        return (leaveTime - joinTime) + currentPlayTime;
    }

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
                if (sender.getServer().getPlayer(name) != null) {
                    final int startTime = (int) (System.currentTimeMillis() / 1000);
                    final int currentTimePlayed = startTime - getStat(name, StatTypes.LAST_JOIN_TIME.id);
                    playTime = currentTimePlayed + getStat(name, StatTypes.PLAY_TIME.id);
                } else {
                    playTime = getStat(name, StatTypes.PLAY_TIME.id);
                }

                String message = transformTime(playTime);

                if (message.equalsIgnoreCase("")) {
                    message = name + " doesn't have any logged playtime yet.";
                    respondToCommand(message, args, sender);
                } else {
                    int thisSession = (int) (System.currentTimeMillis() / 1000) - getStat(name, StatTypes.LAST_JOIN_TIME.id);
                    String thisSessionText = transformTime(thisSession);

                    message = name + " - Playtime: " + message + " | This session: " + thisSessionText;
                    respondToCommand(message, args, sender);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("lastseen")) {
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                if (sender.getServer().getPlayer(name) != null) {
                    sender.getServer().broadcastMessage(name + " is online now!");
                } else {
                    long dateTime = (long) getStat(name, StatTypes.LAST_LEAVE_TIME.id) * 1000;
                    if (dateTime == 0) {
                        String message = name + " hasn't been seen on the server yet.";
                        respondToCommand(message, args, sender);
                    } else {
                        Date date = new Date(dateTime);
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                        format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                        String message = name + " - Last Seen: " + format.format(date);
                        respondToCommand(message, args, sender);
                    }
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("joins")) {
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                String joins = df.format(getStat(name, StatTypes.JOINS.id));
                String message = name + " - Joins: " + joins;
                respondToCommand(message, args, sender);
            }

            return true;
        } else {
            return false;
        }
    }
}
