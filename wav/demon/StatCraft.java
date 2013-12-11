package wav.demon;

import com.avaje.ebean.validation.NotNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import wav.demon.Commands.*;
import wav.demon.Listeners.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class StatCraft extends JavaPlugin {

    public volatile Map<String, Map<Integer, Map<String, Integer>>> statsForPlayers;
    private TimedActivities timedActivities;

    private String timeZone;

    // listeners
    public PlayTime playtime = new PlayTime(this);
    private DeathListener deathListener = new DeathListener(this);
    private BlockListener blockListener = new BlockListener(this);
    private ItemPickUp itemPickUp = new ItemPickUp(this);
    private ItemDrop itemDrop = new ItemDrop(this);
    private ItemsCrafted itemsCrafted = new ItemsCrafted(this);
    private OnFire onFire = new OnFire(this);
    private ToolsBroken toolsBroken = new ToolsBroken(this);
    private ArrowsShot arrowsShot = new ArrowsShot(this);
    private BucketFill bucketFill = new BucketFill(this);
    private BucketEmpty bucketEmpty = new BucketEmpty(this);
    private SleepyTime sleepyTime = new SleepyTime(this);
    private WorldChange worldChange = new WorldChange(this);
    private WordsSpoken wordsSpoken = new WordsSpoken(this);

    // commands
    private ListCommand listCommand = new ListCommand();
    private ResetCommand resetCommand = new ResetCommand(this);
    private PrintData printData = new PrintData(this);
    private UpdateTotals updateTotals = new UpdateTotals(this);

    /**
     *  Config settings
     */
    private boolean enabled;

    // individual stats
    private boolean death;                 /* 1*/
    private boolean death_locations;       /* 2*/
    private boolean block;                 /* 3*/
    private boolean play_time;             /* 4*/
    private boolean last_join_time;        /* 5*/
    private boolean last_leave_time;       /* 6*/
    private boolean joins;                 /* 7*/
    private boolean items_crafted;         /* 8*/
    private boolean on_fire;               /* 9*/
    private boolean world_change;          /*10*/
    private boolean tools_broken;          /*11*/
    private boolean arrows_shot;           /*12*/
    private boolean bucket_fill;           /*13*/
    private boolean bucket_empty;          /*14*/
    private boolean item_drops;            /*15*/
    private boolean item_pickups;          /*16*/
    private boolean bed;                   /*17*/
    private boolean messages_spoken;       /*18*/
    private boolean words_spoken;          /*19*/
    private boolean specific_words_spoken; /*20*/
    private boolean damage_taken;          /*21*/
    private boolean fish_caught;           /*22*/
    private boolean xp_gained;             /*23*/
    private boolean move;                  /*24*/
    private boolean move_type;             /*25*/
    private boolean kills;                 /*26*/
    private boolean jumps;                 /*27*/
    private boolean fallen;                /*28*/
    private boolean egg_throws;            /*29*/
    private boolean chicken_hatches;       /*30*/
    private boolean ender_pearls;          /*31*/
    private boolean animals_bred;          /*32*/
    private boolean tnt_detonated;         /*33*/
    private boolean enchants_done;         /*34*/
    private boolean highest_level;         /*35*/
    private boolean damage_dealt;          /*36*/
    private boolean items_brewed;          /*37*/
    private boolean items_cooked;          /*39*/
    private boolean fires_started;         /*40*/

    // permissions
    private boolean resetOwnStats;
    private String resetAnotherPlayerStats;
    private String resetServerStats;

    // disk writings
    private String totalsUpdating;
    private String statsToDisk;

    // backups
    private boolean backupStats;
    private String backupStatsLocation;
    private String backupStatsInterval;
    private int backupStatsNumber;
    private String backupName;
    /**
     * End config settings
     */

    @Override
    final public void onEnable() {
        // See if the config file exists
        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists()) {
            saveDefaultConfig();
        }

        // load up the config settings
        // should we track anything?
        enabled = getConfig().getBoolean("trackStats");
        if (enabled) {
            // death stuff
            death = getConfig().getBoolean("stats.death");
            death_locations = getConfig().getBoolean("stats.death_locations");
            if (death_locations && !death) {
                System.out.println("StatCraft: death_locations could be enabled because death is false.");
                death_locations = false;
            }

            // blocks
            block = getConfig().getBoolean("stats.block");

            // playtime
            last_join_time = getConfig().getBoolean("stats.last_join_time");
            last_leave_time = getConfig().getBoolean("stats.last_leave_time");
            play_time = getConfig().getBoolean("stats.play_time");
            if (play_time && (!last_join_time || !last_leave_time)) {
                System.out.println("StatCraft: play_time could be enabled because either last_join_time or " +
                        "last_leave_time are false.");
                play_time = false;
            }
            joins = getConfig().getBoolean("stats.joins");
            if (joins && !last_join_time) {
                System.out.println("StatCraft: joins could be enabled because last_join_time is false.");
            }

            // item creation
            items_crafted = getConfig().getBoolean("stats.items_crafted");
            items_brewed = getConfig().getBoolean("stats.items_brewed");
            items_cooked = getConfig().getBoolean("stats.items_cooked");

            // misc
            on_fire = getConfig().getBoolean("stats.on_fire");
            world_change = getConfig().getBoolean("stats.world_change");
            tools_broken = getConfig().getBoolean("stats.tools_broken");
            arrows_shot = getConfig().getBoolean("stats.arrows_shot");

            // buckets!
            bucket_fill = getConfig().getBoolean("stats.bucket_fill");
            bucket_empty = getConfig().getBoolean("stats.bucket_empty");

            // item dropping and picking up
            item_drops = getConfig().getBoolean("stats.item_drops");
            item_pickups = getConfig().getBoolean("stats.item_pickups");

            // sleep!
            bed = getConfig().getBoolean("stats.bed");

            // talking
            messages_spoken = getConfig().getBoolean("stats.messages_spoken");
            words_spoken = getConfig().getBoolean("stats.words_spoken");
            specific_words_spoken = getConfig().getBoolean("stats.specific_words_spoken");
            if (words_spoken && !messages_spoken) {
                System.out.println("StatCraft: words_spoken could not be enabled because messages_spoken is false.");
                words_spoken = false;
            }
            if (specific_words_spoken && !words_spoken) {
                System.out.println("StatCraft: specific_words_spoken could not be enabled because words_spoken is false.");
            }

            // misc
            damage_taken = getConfig().getBoolean("stats.damage_taken");
            fish_caught = getConfig().getBoolean("stats.fish_caught");
            xp_gained = getConfig().getBoolean("stats.xp_gained");

            // movement
            move = getConfig().getBoolean("stats.move");
            move_type = getConfig().getBoolean("stats.move_type");
            if (move_type && !move) {
                System.out.println("StatCraft: move_type could not be enabled because move is false.");
                move_type = false;
            }

            // misc
            kills = getConfig().getBoolean("stats.kills");
            jumps = getConfig().getBoolean("stats.jumps");
            fallen = getConfig().getBoolean("stats.fallen");

            // chickens
            egg_throws = getConfig().getBoolean("stats.egg_throws");
            chicken_hatches = getConfig().getBoolean("stats.chicken_hatches");
            if (chicken_hatches && !egg_throws) {
                System.out.println("StatCraft: chicken_hatches could not be enabled because egg_throws is false.");
            }

            // misc
            ender_pearls = getConfig().getBoolean("stats.ender_pearls");
            animals_bred = getConfig().getBoolean("stats.animals_bred");
            tnt_detonated = getConfig().getBoolean("stats.tnt_detonated");
            enchants_done = getConfig().getBoolean("stats.enchants_done");
            highest_level = getConfig().getBoolean("stats.highest_level");
            damage_dealt = getConfig().getBoolean("stats.damage_dealt");
            fires_started = getConfig().getBoolean("stats.fires_started");

            // Permissions
            resetOwnStats = getConfig().getBoolean("permissions.resetOwnStats");
            resetAnotherPlayerStats = getConfig().getString("permissions.resetAnotherPlayerStats");
            resetServerStats = getConfig().getString("permissions.resetServerStats");
            if (!(resetAnotherPlayerStats.equalsIgnoreCase("op") || resetAnotherPlayerStats.equalsIgnoreCase("user"))) {
                System.out.println("StatCraft: resetAnotherPlayerStats must either be \"op\" or \"user\", defaulting" +
                        "to \"op\"");
                resetAnotherPlayerStats = "op";
            }
            if (!(resetServerStats.equalsIgnoreCase("op") || resetServerStats.equalsIgnoreCase("user"))) {
                System.out.println("StatCraft: resetServerStats must either be \"op\" or \"user\", defaulting" +
                        "to \"op\"");
                resetServerStats = "op";
            }

            // TODO: implement Disk writing and Backups
            // Disk writing
            // TODO: implement input checks for these values
            totalsUpdating = getConfig().getString("writingToDisk.totalsUpdating");
            statsToDisk = getConfig().getString("writingToDisk.statsToDisk");

            // Backups
            // TODO: implement input checks for these values
            backupStats = getConfig().getBoolean("backups.backupStats");
            backupStatsLocation = getConfig().getString("backups.backupStatsLocation");
            backupStatsInterval = getConfig().getString("backups.backupStatsInterval");
            backupStatsNumber = getConfig().getInt("backups.backupStatsNumber");
            backupName = getConfig().getString("backups.backupName");
        }

        // make sure the stats directory exists
        File stat = new File(getDataFolder(), "stats");
        if (stat.exists() && stat.isDirectory()) {
            // it exists and it is a directory, so load up the old stats
            // try to reload the stats, if possible
            try {
                if (reloadStatFiles()) {
                    // yay, it worked
                    System.out.println("StatCraft: Old stats loaded successfully.");
                } else {
                    // something isn't quite right, so start from scratch
                    statsForPlayers = new HashMap<>();
                }
            } catch (IOException e) {
                System.out.println("StatCraft: Something when wrong when trying to read the old stats." +
                        "Could not initialize.");
                e.printStackTrace();
            }
        } else if (!stat.exists()) {
            // the directory doesn't exist, so make a new one and create a new stat HashMap
            stat.mkdir();
            statsForPlayers = new HashMap<>();
        } else if (!stat.isDirectory()) {
            // the file exists, but it's not a directory, so warn the user
            System.out.println("StatCraft: stats file in the plugin data folder is not a directory," +
                    "cannot initialize!");
            enabled = false;
        }

        // set the time zone of the server
        if (getConfig().getString("timezone").equalsIgnoreCase("auto")) {
            TimeZone tz = Calendar.getInstance().getTimeZone();
            timeZone = tz.getDisplayName(false, TimeZone.SHORT);
        } else {
            timeZone = getConfig().getString("timezone");
        }


        if (enabled) {
            String statsEnabled = "";
            // load up the listeners
            if (death) {
                getServer().getPluginManager().registerEvents(deathListener, this);
                statsEnabled = statsEnabled + " death";
                if (death_locations) {
                    statsEnabled = statsEnabled + " death_locations";
                    getCommand("deathlocations").setExecutor(deathListener);
                }

                getCommand("deaths").setExecutor(deathListener);
            }

            if (block) {
                getServer().getPluginManager().registerEvents(blockListener, this);
                statsEnabled = statsEnabled + " block";

                getCommand("blocks").setExecutor(blockListener);
            }

            if (play_time || last_join_time || last_leave_time) {
                getServer().getPluginManager().registerEvents(playtime, this);
                if (last_join_time) {
                    statsEnabled = statsEnabled + " last_join_time";
                    getCommand("lastseen").setExecutor(playtime);
                }
                if (last_leave_time)
                    statsEnabled = statsEnabled + " last_leave_time";
                if (play_time) {
                    statsEnabled = statsEnabled + " playtime";
                    getCommand("playtime").setExecutor(playtime);
                }
            }

            if (item_pickups) {
                getServer().getPluginManager().registerEvents(itemPickUp, this);
                statsEnabled = statsEnabled + " item_pickups";
                getCommand("itempickups").setExecutor(itemPickUp);
            }

            if (item_drops) {
                getServer().getPluginManager().registerEvents(itemDrop, this);
                statsEnabled = statsEnabled + " item_drops";
                getCommand("itemdrops").setExecutor(itemDrop);
            }

            if (items_crafted) {
                getServer().getPluginManager().registerEvents(itemsCrafted, this);
                statsEnabled = statsEnabled + " items_crafted";
                getCommand("itemscrafted").setExecutor(itemsCrafted);
            }

            if (on_fire) {
                getServer().getPluginManager().registerEvents(onFire, this);
                statsEnabled = statsEnabled + " on_fire";
                getCommand("onfire").setExecutor(onFire);
            }

            if (tools_broken) {
                getServer().getPluginManager().registerEvents(toolsBroken, this);
                statsEnabled = statsEnabled + " tools_broken";
                getCommand("toolsbroken").setExecutor(toolsBroken);
            }

            if (arrows_shot) {
                getServer().getPluginManager().registerEvents(arrowsShot, this);
                statsEnabled = statsEnabled + " arrows_shot";
                getCommand("arrowsshot").setExecutor(arrowsShot);
            }

            if (bucket_fill) {
                getServer().getPluginManager().registerEvents(bucketFill, this);
                statsEnabled = statsEnabled + " bucket_fill";
                getCommand("bucketsfilled").setExecutor(bucketFill);
            }

            if (bucket_empty) {
                getServer().getPluginManager().registerEvents(bucketEmpty, this);
                statsEnabled = statsEnabled + " bucket_empty";
                getCommand("bucketsemptied").setExecutor(bucketEmpty);
            }

            if (bed) {
                getServer().getPluginManager().registerEvents(sleepyTime, this);
                statsEnabled = statsEnabled + " bed";
                getCommand("timeslept").setExecutor(sleepyTime);
                getCommand("lastslept").setExecutor(sleepyTime);
            }

            if (world_change) {
                getServer().getPluginManager().registerEvents(worldChange, this);
                statsEnabled = statsEnabled + " world_change";
                getCommand("worldchanges").setExecutor(worldChange);
            }

            if (words_spoken || messages_spoken || specific_words_spoken) {
                getServer().getPluginManager().registerEvents(wordsSpoken, this);
                statsEnabled = statsEnabled + " words_spoken";
                if (words_spoken)
                    getCommand("wordsspoken").setExecutor(wordsSpoken);
                if (messages_spoken)
                    getCommand("messagesspoken").setExecutor(wordsSpoken);
            }

            System.out.println("StatCraft: Successfully enabled:" + statsEnabled);

            // load up commands
            getCommand("list").setExecutor(listCommand);
            getCommand("resetstats").setExecutor(resetCommand);
            getCommand("printdata").setExecutor(printData);
            getCommand("updatetotals").setExecutor(updateTotals);
        }

        timedActivities = new TimedActivities(this);

        System.out.println("StatCraft: Successfully started totals updating: " + timedActivities.startTotalsUpdating(10));

    }

    @Override
    final public void onDisable() {
        saveStatFiles();

        System.out.println("StatCraft: Successfully stopped totals updating: " + timedActivities.stopTotalsUpdating());
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    // TODO: implement staggered stat saving based on number of online players
    @SuppressWarnings("unchecked")
    public boolean saveStatFiles() {
        // set the first iterator
        Iterator baseIt = statsForPlayers.entrySet().iterator();
        while (baseIt.hasNext()) {
            // grab the first pair, then the name and the second map
            Map.Entry pairs = (Map.Entry) baseIt.next();
            String name = (String) pairs.getKey();
            if (!name.equalsIgnoreCase("total")) {
                Map<Integer, Map<String, Long>> secondaryMap = (Map<Integer, Map<String, Long>>) pairs.getValue();
                // set the second iterator off of the second map
                Iterator secondaryIt = secondaryMap.entrySet().iterator();
                while (secondaryIt.hasNext()) {
                    // grab the second pair and the type
                    Map.Entry secondPairs = (Map.Entry) secondaryIt.next();
                    int type = (Integer) secondPairs.getKey();
                    // set gson and grab the json text out of the second map's "value" area
                    Gson gson = new Gson();
                    String json = gson.toJson(secondPairs.getValue());

                    PrintWriter out = null;
                    try {
                        // make sure the directory exists for us to write to
                        File outputDir = new File(this.getDataFolder(), "stats/" + name);
                        if (outputDir.exists()) {
                            out = new PrintWriter(outputDir.toString() + "/" + type);
                        } else {
                            if (outputDir.mkdirs())
                                out = new PrintWriter(outputDir.toString() + "/" + type);
                            else {
                                System.out.println("StatCraft: Fatal error trying to create stat directory");
                                break;
                            }
                        }
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
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean reloadStatFiles() throws IOException {
        statsForPlayers = new HashMap<>();
        if (getDataFolder().exists()) {
            // check the root stats directory
            File statsDir = new File(getDataFolder(), "stats");
            // make sure the directory exists
            if (statsDir.exists()) {
                // check the individual player directories
                for (File player : statsDir.listFiles()) {
                    // get the player's name
                    String name = player.getName();
                    if (!name.equalsIgnoreCase("totals")) {
                        System.out.println("StatCraft: " + name + " found at: " + player.getPath());
                        // put player's name into map, now we need to get the stats themselves
                        statsForPlayers.put(name, new HashMap<Integer, Map<String, Integer>>());
                        for (File type : player.listFiles()) {
                            // grab the statType
                            String statType = type.getName();
                            // set Token type and gson
                            Gson gson = new Gson();
                            Type tokenType = new TypeToken<Map<String, Integer>>(){}.getType();
                            // insert the stats into the map
                            statsForPlayers.get(name).put(Integer.parseInt(statType), (Map<String, Integer>)
                                    gson.fromJson(readFile(type.getPath(), StandardCharsets.UTF_8), tokenType));
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @NotNull
    public TimedActivities getTimedActivities() { return timedActivities; }

    @NotNull
    public String getTimeZone() { return timeZone; }

    @NotNull
    public boolean getResetOwnStats(){ return resetOwnStats; }

    @NotNull
    public String getResetAnotherPlayerStats() { return resetAnotherPlayerStats; }

    @NotNull
    public String getResetServerStats() { return resetServerStats; }

    @NotNull
    public boolean getDeath_locations() { return death_locations; }

    @NotNull
    public boolean getPlay_time() { return play_time; }

    @NotNull
    public boolean getLast_join_time() { return last_join_time; }

    @NotNull
    public boolean getLast_leave_time() { return last_leave_time; }

    @NotNull
    public boolean getSpecific_words_spoken() { return specific_words_spoken; }

    @NotNull
    public boolean getWords_spoken() { return words_spoken; }
}