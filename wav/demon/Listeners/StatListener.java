package wav.demon.Listeners;

import com.avaje.ebean.validation.NotNull;
import com.google.gson.Gson;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import wav.demon.StatCraft;

import javax.annotation.Nullable;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatListener implements Listener {

    protected StatCraft plugin;
    // look, I don't know how big these number are gonna be
    protected DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###,###");

    public StatListener(StatCraft plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * This class is just a super class for all of the listeners and the commands that go with
     * each listener. This class holds a few methods used multiple times by each of the subclasses
     * to clean-up the overall code.
     *
     **/

    // Synchronized method to increment stats on players, this method will be run in a separate
    // asynchronous thread.
    private synchronized void incrementStatToPlayer(int type, String name, String message) {

        // check if they have any stats yet, if not, make one
        if (!plugin.statsForPlayers.containsKey(name))
            plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

        // check if they have any stats for this event yet, if not, make one
        if (!plugin.statsForPlayers.get(name).containsKey(type))
            plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

        // check if they have this particular event yet, if not, set to one. If so, increment it
        if (!plugin.statsForPlayers.get(name).get(type).containsKey(message))
            plugin.statsForPlayers.get(name).get(type).put(message, 1);
        else
            plugin.statsForPlayers.get(name).get(type).put(message, plugin.statsForPlayers.get(name).get(type).get(message) + 1);

        // check to see if they have a total yet. If so, increment it; if not, set to 1
        if (!plugin.statsForPlayers.get(name).get(type).containsKey("total"))
            plugin.statsForPlayers.get(name).get(type).put("total", 1);
        else
            plugin.statsForPlayers.get(name).get(type).put("total", plugin.statsForPlayers.get(name).get(type).get("total") + 1);

        if (plugin.getSaveStatsRealTime()) {
            PrintWriter out = null;
            try {
                // declare the gson for writing the json
                Gson gson = new Gson();
                String json = gson.toJson(plugin.statsForPlayers.get(name).get(type));

                // ensure the output directory exists
                File outputDir = new File(plugin.getDataFolder(), "stats/" + name);

                // check if the directory exists, if not, create it
                if (!outputDir.exists())
                    if (!outputDir.mkdirs()) {
                        plugin.getLogger().severe("Fatal error occurred while trying to save stat files: Could not create directory");
                        return;
                    }

                // set the PrintWriter to the file we are going to write to
                out = new PrintWriter(outputDir.toString() + "/" + type);

                // write the json to the file
                out.println(json);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (out != null)
                    out.close();
            }
        }
    }

    /**
     * FOR MOST CASES YOU SHOULD ONLY USE THE addStat() METHOD, WHICH WILL RUN THIS METHOD IN AN
     * ASYNCHRONOUS THREAD
     * <p>
     * Synchronized method to add stats to players, this method should always be run in a separate
     * asynchronous thread.
     *
     * @param type StatType.id int of whatever stat you want to add
     * @param name Name of the player to add stat to
     * @param data Whatever number to be added to the player's stat
     */
    public synchronized void addStatToPlayer(int type, String name, int data) {

        // check if they have any stats yet, if not, make one
        if (!plugin.statsForPlayers.containsKey(name))
            plugin.statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());

        // check if they have any stats for this event yet, if not, make one
        if (!plugin.statsForPlayers.get(name).containsKey(type))
            plugin.statsForPlayers.get(name).put(type, new HashMap<String, Integer>());

        // add the stat to the total
        plugin.statsForPlayers.get(name).get(type).put("total", data);

        if (plugin.getSaveStatsRealTime()) {
            PrintWriter out = null;
            try {
                // declare the gson for writing the json
                Gson gson = new Gson();
                String json = gson.toJson(plugin.statsForPlayers.get(name).get(type));

                // ensure the output directory exists
                File outputDir = new File(plugin.getDataFolder(), "stats/" + name);

                // check if the directory exists, if not, create it
                if (!outputDir.exists())
                    if (!outputDir.mkdirs()) {
                        plugin.getLogger().severe("Fatal error occurred while trying to save stat files: Could not create directory");
                        return;
                    }

                // set the PrintWriter to the file we are going to write to
                out = new PrintWriter(outputDir.toString() + "/" + type);

                // write the json to the file
                out.println(json);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (out != null)
                    out.close();
            }
        }
    }

    /**
     * Use this method to increment a stat. This will increment a specific stat value to the provided
     * field, and also increment the "total" value
     * <p>
     * This is called by the subclasses to create an asynchronous thread and increment the stats
     * of a player. The incrementation is done by a separate thread to prevent slowdowns of the
     * server as files will be written in the process
     *
     * @param type StatType.id int of whatever stat you want to increment
     * @param name name of the player to increment stat
     * @param message Specific stat to increment
     */
    protected void incrementStat(final int type, final String name, final String message) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                incrementStatToPlayer(type, name, message);
            }
        });
    }

    /**
     * Use this method to add a stat that isn't incremented. This will add whatever data is provided
     * to the "total" field in the provided StatType
     * <p>
     * This is called by the subclasses to create an asynchronous thread and add the stats
     * to a player. The addition is done by a separate thread to prevent slowdowns of the
     * server as files will be written in the process
     * <p>
     * Always use this method when adding stats instead of the addStatToPlayer method. The
     * addStatToPlayer method is only public because of a special case in the ResetStats command
     *
     * @param type StatType.id int of whatever stat you want to add
     * @param name name of the player to add stat
     * @param data Whatever number to be added to the player's stat
     */
    protected void addStat(final int type, final String name, final int data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                addStatToPlayer(type, name, data);
            }
        });
    }

    /**
     * Return a certain stat on a player without trying to reference a key that doesn't exist.
     * This method is used by the command parts of the subclasses. The returned value is the "total"
     * value of the specified stat.
     *
     * @param name Name of the player to get stats from
     * @param type StatType.id int of whatever stat you want to get
     * @return "total" value of specified type
     */
    protected int getStat(String name, int type) {
        int stat;
        if (plugin.statsForPlayers.containsKey(name))
            if (plugin.statsForPlayers.get(name).containsKey(type))
                if (plugin.statsForPlayers.get(name).get(type).containsKey("total"))
                    stat = plugin.statsForPlayers.get(name).get(type).get("total");
                else
                    stat = 0;
            else
                stat = 0;
        else
            stat = 0;

        return stat;
    }

    /**
     * Return the players that the subclass should run the command on.
     *
     * @param sender The CommandSender that was provided in the onCommand method
     * @param args The arguments array that was provided in the onCommand method
     * @return A String array of the players to run the command on
     */
    @Nullable
    protected ArrayList<String> getPlayers(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // if this is run from the console, then a player name must be provided
            if (args.length == 0) {
                // tell them to provide only one name and print usage
                sender.sendMessage("You must name someone from the console!");
                return null;
            }
        }

        ArrayList<String> names = new ArrayList<>();
        if (args.length == 0) {
            names.add(sender.getName());
        } else if (args.length == 1 && args[0].equals("-all")) {
            names.add(sender.getName());
        } else {
            for (String arg : args)
                if (!arg.equals("-all"))
                    names.add(arg);
        }

        return names;
    }

    /**
     * Transform time held in seconds to human readable time
     *
     * @param seconds Length of time interval in seconds
     * @return String of time in a more human readable format, for example 219 seconds would read as "3 minutes, 39 seconds"
     */
    @NotNull
    protected String transformTime(int seconds) {
        // Figure out the playtime in a human readable format
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

        return finalResult;
    }

    /**
     * Respond to the command appropriately, either private or public based on the arguments given.
     * The command will respond publicly if the "-all" argument is given, in all other cases the response will be private.
     *
     * @param message The message that the command outputs
     * @param args The arguments for the command
     * @param sender The CommandSender that was provided in the onCommand method
     */
    protected void respondToCommand(String message, String[] args, CommandSender sender) {
        boolean publicCmd = false;
        for (String arg : args)
            if (arg.equals("-all"))
                publicCmd = true;

        if (publicCmd)
            sender.getServer().broadcastMessage(message);
        else
            sender.sendMessage(message);
    }
}