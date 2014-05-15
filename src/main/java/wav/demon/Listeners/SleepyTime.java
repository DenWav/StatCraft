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

import java.text.SimpleDateFormat;
import java.util.*;

public class SleepyTime extends StatListener {

    public SleepyTime(StatCraft plugin) { super(plugin); }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);

        addStat(StatTypes.ENTER_BED.id, name, currentTime);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        final String name = event.getPlayer().getName();
        final int currentTime = (int) (System.currentTimeMillis() / 1000);
        addStat(StatTypes.LEAVE_BED.id, name, currentTime);

        addStat(StatTypes.TIME_SLEPT.id, name, calculateTimeInterval(name, currentTime));
    }


    // NOTE: Only call this method on PlayerBedLeaveEvent!
    private int calculateTimeInterval(String name, final int leaveBed) {
        final int currentSleepTime = getStat(name, StatTypes.TIME_SLEPT.id);
        final int enterBed = getStat(name, StatTypes.ENTER_BED.id);

        return (leaveBed - enterBed) + currentSleepTime;
    }

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
                if (getStat(name, StatTypes.ENTER_BED.id) > getStat(name, StatTypes.LEAVE_BED.id)) {
                    final int startTime = (int) (System.currentTimeMillis() / 1000);
                    final int currentTimeSlept = startTime - getStat(name, StatTypes.ENTER_BED.id);
                    timeSlept = currentTimeSlept + getStat(name, StatTypes.TIME_SLEPT.id);
                } else {
                    timeSlept = getStat(name, StatTypes.TIME_SLEPT.id);
                }

                String message = transformTime(timeSlept);

                if (message.equalsIgnoreCase("")) {
                    message = "§c" + name + "§f hasn't slept yet.";
                    respondToCommand(message, args, sender, StatTypes.TIME_SLEPT);
                } else {
                    message = "§c" + name + "§f - Time Slept: " + message;
                    respondToCommand(message, args, sender, StatTypes.TIME_SLEPT);
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
                if (getStat(name, StatTypes.ENTER_BED.id) > getStat(name, StatTypes.LEAVE_BED.id)) {
                    String message = "§c" + name + "§f is sleeping now!";
                    respondToCommand(message, args, sender, null);
                } else {
                    long dateTime = (long) getStat(name, StatTypes.LEAVE_BED.id) * 1000;
                    if (dateTime == 0) {
                        String message = "§c" +  name + "§f hasn't slept yet.";
                        respondToCommand(message, args, sender, null);
                    } else {
                        Date date = new Date(dateTime);
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, hh:mm aa yyyy");
                        format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
                        String message = "§c" + name + "§f - Last Slept: " + format.format(date);
                        respondToCommand(message, args, sender, null);
                    }
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
            Map<String, Integer> sortableMap = Collections.synchronizedMap(new ValueComparableMap<String, Integer>(Ordering.from(Collections.reverseOrder())));

            for (Map.Entry<String, HashMap<Integer, HashMap<String, Integer>>> pairs : plugin.statsForPlayers.entrySet()) {
                String name = pairs.getKey();
                if (!name.equalsIgnoreCase("total")) {
                    HashMap<Integer, HashMap<String, Integer>> playerMap = pairs.getValue();
                    Map<String, Integer> typeMap = playerMap.get(type.id);
                    if (typeMap != null) {
                        Integer total = typeMap.get("total");
                        if (total != null) {
                            sortableMap.put(name, total);
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
