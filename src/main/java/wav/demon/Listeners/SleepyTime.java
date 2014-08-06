package wav.demon.Listeners;

import com.google.common.collect.Ordering;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import wav.demon.StatCraft;
import wav.demon.StatTypes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class SleepyTime extends StatListener {

    public SleepyTime(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        addStat(StatTypes.ENTER_BED.id, uuid, currentTime);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);
        addStat(StatTypes.LEAVE_BED.id, uuid, currentTime);

        addStat(StatTypes.TIME_SLEPT.id, uuid, calculateTimeInterval(uuid, currentTime));
    }


    // NOTE: Only call this method on PlayerBedLeaveEvent!
    private int calculateTimeInterval(String uuid, final int leaveBed) {
        final int currentSleepTime = getStat(uuid, StatTypes.TIME_SLEPT.id);
        final int enterBed = getStat(uuid, StatTypes.ENTER_BED.id);

        return (leaveBed - enterBed) + currentSleepTime;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("timeslept")) {
            // determine who to list sleepy time for
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            // now list them
            int timeSlept;
            for (String name : names) {
                try {
                    if (getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.ENTER_BED.id) >
                            getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.LEAVE_BED.id)) {
                        final int startTime = (int) (System.currentTimeMillis() / 1000);
                        final int currentTimeSlept = startTime - getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.ENTER_BED.id);
                        timeSlept = currentTimeSlept + getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.TIME_SLEPT.id);
                    } else {
                        timeSlept = getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.TIME_SLEPT.id);
                    }

                    String message = transformTime(timeSlept);

                    if (message.equalsIgnoreCase("")) {
                        message = "§c" + name + "§f hasn't slept yet.";
                        respondToCommand(message, args, sender, StatTypes.TIME_SLEPT);
                    } else {
                        message = "§c" + name + "§f - Time Slept: " + message;
                        respondToCommand(message, args, sender, StatTypes.TIME_SLEPT);
                    }
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f - Time Slept: 0", args, sender, StatTypes.TIME_SLEPT);
                }
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("lastslept")) {
            ArrayList<String> names = getPlayers(sender, args);
            if (names == null)
                return false;

            for (String arg : args) {
                if (arg.startsWith("-top")) {
                    sender.sendMessage("\"-top\" is not allowed with the lastslept command.");

                    return true;
                }
            }

            for (String name : names) {
                try {
                    if (getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.ENTER_BED.id) >
                            getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.LEAVE_BED.id)) {
                        String message = "§c" + name + "§f is sleeping now!";
                        respondToCommand(message, args, sender, null);
                    } else {
                        long dateTime = (long) getStat(plugin.players.getValueFromKey(name).toString(), StatTypes.LEAVE_BED.id) * 1000;
                        if (dateTime == 0) {
                            String message = "§c" + name + "§f hasn't slept yet.";
                            respondToCommand(message, args, sender, null);
                        } else {
                            Date date = new Date(dateTime);
                            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                            format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                            String message = "§c" + name + "§f - Last Slept: " + format.format(date);
                            respondToCommand(message, args, sender, null);
                        }
                    }
                } catch (NullPointerException e) {
                    respondToCommand("§c" + name + "§f hasn't slept yet.", args, sender, null);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected String typeFormat(int value, StatTypes type) {
        return transformTime(value);
    }

    @Override
    protected String typeLabel(StatTypes type) {
        return "Time Slept";
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
            // this is a -top command, so get the list of the top people for the stats
            // create a ValueComparableMap to sort in reverse order, so the largest numbers are on top
            Map<String, Integer> sortableMap = Collections.synchronizedMap(new ValueComparableMap<String, Integer>(Ordering.from(Collections.reverseOrder())));
            // parse through the stats directory to find the individual stats
            // get the list of directories in the stats/ directory, these will be player directories
            File[] files = plugin.getStatsDir().listFiles();
            // make sure the directory list isn't null for some reason
            if (files != null) {
                // loop through each player directory looking for the specific stat
                for (File name : files) {
                    // there is a "totals" directory here, ignore it
                    if (!name.getName().equalsIgnoreCase("totals")) {
                        // find the stat file that matches the specified stat type
                        File typeFile = new File(name, type.id + "");
                        // create a map of the json file of the specified type
                        HashMap<String, Integer> map = getMapFromFile(typeFile);
                        // make sure the map isn't null
                        if (map != null) {
                            // find the "total" value in the stat
                            Integer total = map.get("total");
                            // make sure the total value isn't null
                            if (total != null) {
                                // place the value of "total" in the ValueComparableMap
                                sortableMap.put(name.getName(), total);
                            }
                        }
                    }
                }

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (getStat(player.getName(), StatTypes.ENTER_BED.id) > getStat(player.getName(), StatTypes.LEAVE_BED.id)) {
                        int timeSlept;
                        final int startTime = (int) (System.currentTimeMillis() / 1000);
                        final int currentTimeSlept = startTime - getStat(player.getName(), StatTypes.ENTER_BED.id);
                        timeSlept = currentTimeSlept + getStat(player.getName(), StatTypes.TIME_SLEPT.id);

                        sortableMap.put(player.getName(), timeSlept);
                    }
                }

                String output = typeLabel(type) + " - ";
                Iterator iterator = sortableMap.entrySet().iterator();
                for (int i = 1; i <= topNumber; i++) {
                    if (iterator.hasNext()) {
                        Map.Entry<String, Integer> sortedMapEntry = (Map.Entry<String, Integer>) iterator.next();

                        String name = plugin.players.getKeyFromValue(UUID.fromString(sortedMapEntry.getKey()));
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
            } else {
                // if the player directory listing returns null, then something went wrong in the command
                // the issue is more than likely a permissions issue and out of the control of this plugin
                sender.sendMessage("There was an error processing that command.");
            }
        }
    }
}
