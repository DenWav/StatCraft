package wav.demon.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SleepyTime extends StatListener implements CommandExecutor {

    private StatCraft plugin;

    public SleepyTime(StatCraft plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) (System.nanoTime() / 1000);

        addStat(StatTypes.ENTER_BED.id, name, currentTime);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) (System.nanoTime() / 1000);
        addStat(StatTypes.LEAVE_BED.id, name, currentTime);

        addStat(StatTypes.TIME_SLEPT.id, name, calculateTimeInterval(name, currentTime));
    }


    // NOTE: Only call this method on PlayerBedLeaveEvent!
    private int calculateTimeInterval(String name, final int leaveBed) {
        final int currentSleepTime = getStat(name, StatTypes.TIME_SLEPT.id);
        final int enterBed = getStat(name, StatTypes.ENTER_BED.id);

        int playTime = (leaveBed - enterBed) + currentSleepTime;
        return playTime;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("timeslept")) {
            // determine who to list sleepy time for
            String[] names = getPlayers(sender, args);
            if (names == null)
                return false;

            // now list them
            int timeSlept;
            for (String name : names) {
                if (getStat(name, StatTypes.ENTER_BED.id) > getStat(name, StatTypes.LEAVE_BED.id)) {
                    final int startTime = (int) (System.currentTimeMillis() / 1000);
                    final int currentTimeSlept = startTime - getStat(name, StatTypes.LEAVE_BED.id);
                    timeSlept = currentTimeSlept + getStat(name, StatTypes.TIME_SLEPT.id);
                } else {
                    timeSlept = getStat(name, StatTypes.TIME_SLEPT.id);
                }

                String message = transformTime(timeSlept);

                if (message.equalsIgnoreCase(""))
                    sender.getServer().broadcastMessage(name + " hasn't slept yet.");
                else
                    sender.getServer().broadcastMessage(name + " - Time Slept: " + message);
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("lastslept")) {
            String[] names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String name : names) {
                if (getStat(name, StatTypes.ENTER_BED.id) > getStat(name, StatTypes.LEAVE_BED.id)) {
                    sender.getServer().broadcastMessage(name + " is sleeping now!");
                } else {
                    long dateTime = (long) getStat(name, StatTypes.LEAVE_BED.id) * 1000;
                    if (dateTime == 0) {
                        sender.getServer().broadcastMessage(name + " hasn't slept yet.");
                    } else {
                        Date date = new Date(dateTime);
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                        format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                        sender.getServer().broadcastMessage(name + " - Last Slept: " + format.format(date));
                    }
                }
            }
            return true;
        }
        return false;
    }
}
