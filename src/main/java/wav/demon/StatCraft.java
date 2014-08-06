package wav.demon;

import com.avaje.ebean.validation.NotNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import wav.demon.Commands.*;
import wav.demon.Listeners.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DemonWav
 */
public class StatCraft extends JavaPlugin {

    public volatile HashMap<String, HashMap<Integer, HashMap<String, Integer>>> statsForPlayers;
    public volatile UniqueHashMap<String, UUID> players;

    private Object threadLock = new Object();

    private File playersFile = new File(getDataFolder(), "players.json");
    private File statsDir = new File(getDataFolder(), "stats");

    private TimedActivities timedActivities;

    private String timeZone;

    private HashMap<String, Integer> lastFireTime = new HashMap<>();
    private HashMap<String, Integer> lastDrownTime = new HashMap<>();
    private HashMap<String, Integer> lastPoisonTime = new HashMap<>();

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
    private DamageTaken damageTaken = new DamageTaken(this);
    private DamageDealt damageDealt = new DamageDealt(this);
    private FishCaught fishCaught = new FishCaught(this);
    private XpGained xpGained = new XpGained(this);
    private KillListener killListener = new KillListener(this);
    public  HighestLevel highestLevel = new HighestLevel(this);
    private TabComplete tabComplete = new TabComplete(this);

    // commands
    private ListCommand listCommand = new ListCommand();
    private ResetCommand resetCommand = new ResetCommand(this);
    private UpdateTotals updateTotals = new UpdateTotals(this);
    private SaveStats saveStats = new SaveStats(this);
    private ForceBackup forceBackup = new ForceBackup(this);

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
    private boolean on_fire_announce;
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
    private boolean drowning_announce;
    private boolean poison_announce;
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
    private boolean mined_ores;            /*41*/
    private boolean tab_complete;          /*42*/

    // permissions
    private boolean resetOwnStats;
    private String resetAnotherPlayerStats;
    private String resetServerStats;

    // disk writings
    private String totalsUpdating;
    private boolean totalsUpdatingEnabled = true;
    private int totalsUpdatingMilliSec;
    private String statsToDisk;
    private boolean saveStatsRealTime = false;
    private int statsToDiskMilliSec;

    // backups
    private boolean backupStats;
    private String backupStatsLocation;
    private String backupStatsInterval;
    private int backupStatsNumber;
    private String backupName;
    private int backupMilliSec;
    private ArrayList<String> backups = new ArrayList<>();
    private int backupNumber;
    /**
     * End config settings
     */

    @Override
    @SuppressWarnings("unchecked")
    final public void onEnable() {

        // See if the config file exists
        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists()) {
            saveDefaultConfig();
        }


        // Get what info we can from the players.json file in the plugin directory. If it doesn't exist, it is assumed
        // that this plugin was either just installed or is updated from a pre-UUID version, so nothing needs to be done.
        // A new players.json file will be created from the players that are online, and it will be re-written whenever
        // a new player joins that isn't already in the players.json file. Whenever a player joins (or is found from the
        // players that are already online immediately after this) and they are still using their nickname as the stats
        // file identifier, the plugin will convert it to their UUID. The plugin simply keeps a log of the players'
        // nicknames and UUIDs so commands and command outputs can still use nicknames, while still storing the stats
        // under the player's UUID.
        readPlayersFile();

        // Now we will write to the players.json file any added or changed nickname-UUID pairs. If a nickname-UUID pair
        // is changed, and the stats-directory is already changed to the UUID of the player, then nothing needs to be
        // done to the player's stats directory. If a nickname-UUID pair is added, and the player's stats directory still
        // uses their nickname, then the stats directory will need to be renamed to their UUID. If a nickname-UUID pair
        // is changed, and their old nickname is on file, but their stats directory still uses their old nickname, we
        // will need to rename it to the UUID. If a nickname-UUID pair is changed, and we do not have the old nickname
        // on file, then there is nothing we can do, since there is no way of knowing exactly which nickname should be
        // assigned to the player, so the plugin will assume it is a new player, and will assign a new stat-directory
        // based on their UUID.

        // just a security check
        if (players == null)
            players = UniqueHashMap.create(String.class, UUID.class);
        writePlayersFile();


        // we are going to see if there are any people online
        // if there are people online, compare them to the players HashMap
        // we want to make sure the UUIDs and player names all match up correctly
        // If there are any players that are not in the players HashMap, we will add them and
        // write it to the players.json
        if (players != null && getServer().getOnlinePlayers() != null)
        for (Player p : getServer().getOnlinePlayers()) {
            if (players.containsValue(p.getUniqueId()) && !players.containsKey(p.getName())) {
                players.removeValue(p.getUniqueId());
            }
            try {
                if (!players.containsKey(p.getName()) || !players.containsValue(p.getUniqueId()))
                    players.put(p.getName(), p.getUniqueId());
            } catch (ValueNotUniqueException e) {
                e.printStackTrace();
            }
        }

        // set the time zone of the server
        if (getConfig().getString("timezone").equalsIgnoreCase("auto")) {
            TimeZone tz = Calendar.getInstance().getTimeZone();
            timeZone = tz.getDisplayName(false, TimeZone.SHORT);
        } else {
            timeZone = getConfig().getString("timezone");
        }

          //////////////////////
         //     CONFIG       //
        //////////////////////
        // should we track anything?
        enabled = getConfig().getBoolean("trackStats");
        if (enabled) {
            getConfigSettings();
        }

        // make sure the stats directory exists
        File stat = new File(getDataFolder(), "stats");
        if (stat.exists() && stat.isDirectory()) {
            // it exists and it is a directory, so load up the old stats
            // try to reload the stats, if possible
            statsForPlayers = new HashMap<>();
            try {
                if (checkStatFiles()) {
                    // yay, it worked
                    getLogger().info("Old stats checked successfully.");
                } else {
                    // something went wrong, but I don't know exactly what happened
                    getLogger().warning("There was an error when checking old stats.");
                }
            } catch (IOException e) {
                getLogger().severe("Something when wrong when trying to read the old stats.");
                e.printStackTrace();
                enabled = false;
            }
        } else if (!stat.exists()) {
            // the directory doesn't exist, so make a new one and create a new stat HashMap
            if (!stat.mkdirs()) {
                getLogger().severe("Fatal error when trying to create " + stat.toString() + ". Possibly " +
                        "a permissions issue?");
                enabled = false;
            } else {
                statsForPlayers = new HashMap<>();
            }
        } else if (!stat.isDirectory()) {
            // the file exists, but it's not a directory, so warn the user
            getLogger().severe("Stats file in the plugin data folder is not a directory," +
                    "cannot initialize!");
            enabled = false;
        }

          ////////////////////
         //    LISTENERS   //
        ////////////////////
        if (enabled) {
            createListeners();
        }

        timedActivities = new TimedActivities(this);

        if (totalsUpdatingEnabled)
            getLogger().info("Successfully started totals updating (" + totalsUpdating + "): "
                + timedActivities.startTotalsUpdating(totalsUpdatingMilliSec));
        if (!saveStatsRealTime)
            getLogger().info("Successfully started delayed stat saving (" + statsToDisk + "): "
                    + timedActivities.startStatsToDisk(statsToDiskMilliSec));
        if (backupStats)
            getLogger().info("Successfully started backups (" + backupStatsInterval + "): "
                    + timedActivities.startBackup(backupMilliSec));
    }

    @SuppressWarnings("unchecked")
    private void getConfigSettings() {
        // death stuff
        death = getConfig().getBoolean("stats.death");
        death_locations = getConfig().getBoolean("stats.death_locations");
        if (death_locations && !death) {
            getLogger().warning("death_locations could be enabled because death is false.");
            death_locations = false;
        }

        // blocks
        block = getConfig().getBoolean("stats.block");
        mined_ores = getConfig().getBoolean("stats.mined_ores");
        if (!block && mined_ores) {
            getLogger().warning("mined_ores could not be enabled because block is false.");
            mined_ores = false;
        }

        // playtime
        last_join_time = getConfig().getBoolean("stats.last_join_time");
        last_leave_time = getConfig().getBoolean("stats.last_leave_time");
        play_time = getConfig().getBoolean("stats.play_time");
        if (play_time && (!last_join_time || !last_leave_time)) {
            getLogger().warning("play_time could not be enabled because either last_join_time or " +
                    "last_leave_time are false.");
            play_time = false;
        }
        joins = getConfig().getBoolean("stats.joins");
        if (joins && !last_join_time) {
            getLogger().warning("joins could not be enabled because last_join_time is false.");
        }

        // item creation
        items_crafted = getConfig().getBoolean("stats.items_crafted");
        items_brewed = getConfig().getBoolean("stats.items_brewed");
        items_cooked = getConfig().getBoolean("stats.items_cooked");

        // misc
        on_fire = getConfig().getBoolean("stats.on_fire");
        on_fire_announce = getConfig().getBoolean("stats.on_fire_announce");
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
            getLogger().warning("words_spoken could not be enabled because messages_spoken is false.");
            words_spoken = false;
        }
        if (specific_words_spoken && !words_spoken) {
            getLogger().warning("specific_words_spoken could not be enabled because words_spoken is false.");
        }

        tab_complete = getConfig().getBoolean("stats.tab_complete");

        // misc
        damage_taken = getConfig().getBoolean("stats.damage_taken");
        drowning_announce = getConfig().getBoolean("stats.drowning_announce");
        poison_announce = getConfig().getBoolean("stats.poison_announce");
        fish_caught = getConfig().getBoolean("stats.fish_caught");
        xp_gained = getConfig().getBoolean("stats.xp_gained");

        // movement
        move = getConfig().getBoolean("stats.move");
        move_type = getConfig().getBoolean("stats.move_type");
        if (move_type && !move) {
            getLogger().warning("move_type could not be enabled because move is false.");
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
            getLogger().warning("chicken_hatches could not be enabled because egg_throws is false.");
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
            getLogger().warning("resetAnotherPlayerStats must either be \"op\" or \"user\", defaulting" +
                    "to \"op\"");
            resetAnotherPlayerStats = "op";
        }
        if (!(resetServerStats.equalsIgnoreCase("op") || resetServerStats.equalsIgnoreCase("user"))) {
            getLogger().warning("resetServerStats must either be \"op\" or \"user\", defaulting" +
                    "to \"op\"");
            resetServerStats = "op";
        }

        /////////////////////
        //   Disk writing  //
        /////////////////////
        totalsUpdating = getConfig().getString("writingToDisk.totalsUpdating");
        totalsUpdatingMilliSec = parseTime(totalsUpdating);

        if (totalsUpdatingMilliSec == -1) {
            totalsUpdatingEnabled = false;
            getLogger().warning("Totals Updating could not be enabled, \"" + totalsUpdating + "\" is invalid.");
        }

        statsToDisk = getConfig().getString("writingToDisk.statsToDisk");
        if (statsToDisk.equalsIgnoreCase("real-time"))
            saveStatsRealTime = true;
        else
            statsToDiskMilliSec = parseTime(statsToDisk);

        if (statsToDiskMilliSec == -1) {
            statsToDiskMilliSec = 30 * 1000;
            getLogger().warning("StatsToDisk could not be enabled correctly, \"" + statsToDisk + "\"" +
                    " is invalid. Defaulting to 30 seconds delay.");
        }

        ///////////////
        //  Backups  //
        ///////////////
        backupStats = getConfig().getBoolean("backups.backupStats");
        if (backupStats) {
            backupStatsLocation = getConfig().getString("backups.backupStatsLocation");

            backupName = getConfig().getString("backups.backupName");

            backupStatsInterval = getConfig().getString("backups.backupStatsInterval");
            backupMilliSec = parseTime(backupStatsInterval);

            if (backupMilliSec == -1) {
                backupMilliSec = 24 * 60 * 60 * 1000;
                getLogger().warning("Backups could not be enabled correctly, \"" + backupStatsInterval + "\"" +
                        " is invalid. Defaulting to once every 24 hours.");
            }

            backupStatsNumber = getConfig().getInt("backups.backupStatsNumber");

            // get the current backup number
            Scanner input = null;
            try {
                File backupNumberFile = new File(getDataFolder(), "backup-number");
                input = new Scanner(backupNumberFile);
                backupNumber = input.nextInt();
            } catch (FileNotFoundException e) {
                backupNumber = 1;
            } finally {
                if (input != null)
                    input.close();
            }

            // get the current backups from disk
            File backupArray = new File(getDataFolder(), "backup-array");
            if (backupArray.exists()) {
                ObjectInputStream ois = null;
                try {
                    FileInputStream fis = new FileInputStream(backupArray);
                    ois = new ObjectInputStream(fis);
                    Object array = ois.readObject();
                    backups = (ArrayList<String>) array;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (ois != null)
                            ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createListeners() {
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

            if (mined_ores) {
                statsEnabled = statsEnabled + " mined_ores";
                getCommand("mined").setExecutor(blockListener);
            }
        }

        if (play_time || last_join_time || last_leave_time || joins) {
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
            if (joins) {
                statsEnabled = statsEnabled + " joins";
                getCommand("joins").setExecutor(playtime);
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

        if (damage_taken) {
            getServer().getPluginManager().registerEvents(damageTaken, this);
            statsEnabled = statsEnabled + " damage_taken";
            getCommand("damagetaken").setExecutor(damageTaken);
        }

        if (damage_dealt) {
            getServer().getPluginManager().registerEvents(damageDealt, this);
            statsEnabled = statsEnabled + " damage_dealt";
            getCommand("damagedealt").setExecutor(damageDealt);
        }

        if (fish_caught) {
            getServer().getPluginManager().registerEvents(fishCaught, this);
            statsEnabled = statsEnabled + " fish_caught";
            getCommand("fishcaught").setExecutor(fishCaught);
        }

        if (xp_gained) {
            getServer().getPluginManager().registerEvents(xpGained, this);
            statsEnabled = statsEnabled + " xp_gained";
            getCommand("xpgained").setExecutor(xpGained);
        }

        if (kills) {
            getServer().getPluginManager().registerEvents(killListener, this);
            statsEnabled = statsEnabled + " kills";
            getCommand("kills").setExecutor(killListener);
        }

        if (highest_level) {
            getServer().getPluginManager().registerEvents(highestLevel, this);
            statsEnabled = statsEnabled + " highest_level";
            getCommand("highestlevel").setExecutor(highestLevel);
        }

        if (tab_complete) {
            getServer().getPluginManager().registerEvents(tabComplete, this);
            statsEnabled = statsEnabled + " tab_complete";
            getCommand("tabcompletes").setExecutor(tabComplete);
        }

        getLogger().info("Successfully enabled:" + statsEnabled);

        // load up commands
        getCommand("list").setExecutor(listCommand);
        getCommand("resetstats").setExecutor(resetCommand);
        getCommand("updatetotals").setExecutor(updateTotals);
        getCommand("savestats").setExecutor(saveStats);
        getCommand("forcebackup").setExecutor(forceBackup);
    }

    @Override
    final public void onDisable() {
        if (!getSaveStatsRealTime())
            saveStatFiles();

        if (!timedActivities.totalUpdateNull())
            getLogger().info("Successfully stopped totals updating: " + timedActivities.stopTotalsUpdating());

        if (!timedActivities.statsToDiskNull())
            getLogger().info("Successfully stopped delayed stat saving: " + timedActivities.stopStatsToDisk());

        if (!timedActivities.backupNull())
            getLogger().info("Successfully stopped backups: " + timedActivities.stopBackup());
    }

    /**
     * Reads the text from a file and returns it as a String
     *
     * @param path The path to the file as a String
     * @param encoding The encoding of the file
     * @return The contents of the file as a String
     * @throws IOException
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    /**
     * Saves all of the stats saved in memory to the disk.
     * <p>
     * The stats are only saved as single JSON files by the top-level HashMap of statsForPlayers. With the structure of
     * statsForPlayers as: HashMap&lt;String, HashMap&lt;Integer, HashMap&lt;String, Integer&gt;&gt;&gt;, the first String key of the
     * primary HashMap is the name of the player, and is saved as a directory by the player name.
     * <p>
     * In the directory, the data is now stored as a HashMap with structure: HashMap&lt;Integer, HashMap&lt;String, Integer&gt;&gt;.
     * The integer value of the new primary HashMap is the stat type, and is saved as a text file with that integer value
     * as the name. Inside the text file is the HashMap&lt;String, Integer&gt;, saved in a JSON format.
     *
     * @return Whether the saving is successful or not
     *
     * @see java.util.HashMap
     */
    @SuppressWarnings("unchecked")
    public boolean saveStatFiles() {
        // we need this inside the asynchronous thread

        // set the first iterator
        if (statsForPlayers != null)
        for (Map.Entry<String, HashMap<Integer, HashMap<String, Integer>>> pairs : statsForPlayers.entrySet()) {
            // grab the first pair, then the name and the second map
            String name = pairs.getKey();
            if (!name.equalsIgnoreCase("total")) {
                HashMap<Integer, HashMap<String, Integer>> secondaryMap = pairs.getValue();
                // set the second iterator off of the second map
                for (Map.Entry<Integer, HashMap<String, Integer>> secondPairs : secondaryMap.entrySet()) {
                    // grab the second pair and the type
                    int type = secondPairs.getKey();
                    // set gson and grab the json text out of the second map's "value" area
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson.toJson(secondPairs.getValue());

                    PrintWriter out = null;
                    try {
                        synchronized (getThreadLock()) {
                            // make sure the directory exists for us to write to
                            File outputDir = new File(getStatsDir(), name);
                            if (outputDir.exists()) {
                                out = new PrintWriter(outputDir.toString() + "/" + type);
                            } else {
                                if (outputDir.mkdirs()) {
                                    out = new PrintWriter(outputDir.toString() + "/" + type);
                                } else {
                                    getLogger().warning("Fatal error trying to create stat directory");
                                    break;
                                }
                            }
                            // write the json to the file
                            out.println(json);
                        }
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

    /**
     * Verifies the stat files from the disk are valid
     * <p>
     * The exact format of the stats saved on the disk is discussed in the documentation for
     * {@link #saveStatFiles() saveStatFiles()}.
     *
     * @return Whether the check was successful or not
     * @throws IOException
     *
     * @see java.util.HashMap
     * @see #saveStatFiles()
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public boolean checkStatFiles() throws IOException {
        if (getDataFolder().exists()) {
            // make sure the directory exists
            if (statsDir.exists()) {
                // check the individual player directories
                if (statsDir.listFiles() != null)
                for (File player : statsDir.listFiles()) {
                    // get the player's name
                    String name = player.getName();
                    if (!name.equalsIgnoreCase("totals")) {
                        if (player.listFiles() != null)
                        for (File type : player.listFiles()) {
                            // grab the statType
                            String statType = type.getName();
                            // set Token type and gson
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            Type tokenType = new TypeToken<HashMap<String, Integer>>(){}.getType();
                            // insert the stats into the map to verify it works
                            PrintWriter pw = null;
                            try {
                                Integer.parseInt(statType);
                                HashMap<String, Integer> map = gson.fromJson(removeDuplicateFields(
                                        readFile(type.getPath(), StandardCharsets.UTF_8), statType, name),
                                        tokenType);
                                String json = gson.toJson(map);
                                pw = new PrintWriter(type);
                                pw.println(json);
                            } catch (Exception e) {
                                getLogger().severe(type + " stat for " + name + " could not be loaded successfully, deleting " + type + ".");
                                e.printStackTrace();
                                type.delete();
                            } finally {
                                if (pw != null)
                                    pw.close();
                            }
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

    /**
     * Remove duplicate entries from a simple JSON format.
     * The input must only be a simple String:Integer JSON format, this method does not handle anything more complicated.
     * <p>
     * For example, this would work: {"One":1,"Two":2,"One":3}
     * <p>
     * This method would return: {"Two":2,"One":3}, as it only keeps the highest value for each duplicate key.
     * It does this because after a lot of testing I came to determine that whenever a duplicate key did pop up, it was
     * always an outlier and did not actually contribute to the total value.
     * <p>
     * If a more complicated input were to be given, with values holding strings, or arrays, or anything else besides
     * something like the example given, the method would not work correctly.
     *
     * @param in The input JSON, which must be a simple String:Integer set
     * @param type The type of stat, used only for logging
     * @param name The name of the player the stat belongs to, used only for logging
     *
     * @return The input JSON, but with only the highest value of each duplicate key kept. If no duplicate keys were
     * found, then it returns exactly what was input.
     */
    public String removeDuplicateFields(String in, String type, String name) {
        // for the use of this method in this plugin, there will never be
        // a more complex JSON format than simply keys and values
        in = in.replaceAll("[\\{}]", "");
        ArrayList<String> sets = new ArrayList<>(Arrays.asList(in.split(",")));
        HashMap<String, Integer> counter = new HashMap<>();

        // grab each id
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(in);

        // count each id occurrence
        while (m.find()) {
            String found = m.group(1);
            Integer value = counter.get(found);
            counter.put(found, value == null ? 1 : value + 1);
        }

        // loop through the occurrence map
        for (Map.Entry<String, Integer> pair : counter.entrySet()) {
            if (pair.getValue() > 1) {
                // we have a duplicate key, so keep only the key with the larger value
                int max = 0;
                for (int i = 0; i < pair.getValue(); i++) {
                    // find the duplicates
                    for (int j = sets.size() - 1; j >= 0; j--) {
                        if (sets.get(j).startsWith("\"" + pair.getKey() + "\"")) {
                            int entry = Integer.parseInt(sets.get(j).substring(sets.get(j).lastIndexOf(":") + 1));
                            // only keep the largest value
                            if (entry > max) max = entry;
                            sets.remove(j);
                        }
                    }
                }
                // put the largest value back in
                sets.add("\"" + pair.getKey() + "\":" + max);
                getLogger().warning("Removed duplicate key: " + pair.getKey() + " in " + type + " for " + name);
            }
        }

        String result = "";
        for (String s : sets) {
            result = result + s + ",";
        }
        result = "{" + result.substring(0, result.length() - 1) + "}";

        return result;
    }

    /**
     * Returns the amount of time given in milliseconds. The syntax for the input must contain only an integer followed
     * by only one of 5 lowercase letters: s, m, h, d, w
     * <p>
     * The integer is the value of the time, and the letter is the unit. The letters stand for seconds, minutes, hours,
     * days, and weeks, respectively.
     *
     * @param time The string entry of exactly one integer followed immediately by s, m, h, d, or w
     * @return the amount of time given in milliseconds
     */
    private static int parseTime(String time) {
        char timeUnit;
        if (time.endsWith("s")) {
            time = time.replace("s", "");
            timeUnit = 's';
        } else if (time.endsWith("m")) {
            time = time.replace("m", "");
            timeUnit = 'm';
        } else if (time.endsWith("h")) {
            time = time.replace("h", "");
            timeUnit = 'h';
        } else if (time.endsWith("d")) {
            time = time.replace("d", "");
            timeUnit = 'd';
        } else if (time.endsWith("w")) {
            time = time.replace("w", "");
            timeUnit = 'w';
        } else {
            return -1;
        }

        int timeInt;
        try {
            timeInt = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            return -1;
        }

        int timeMilliSec = 0;
        switch (timeUnit) {
            case 's':
                timeMilliSec = timeInt * 1000;
                break;
            case 'm':
                timeMilliSec = timeInt * 60 * 1000;
                break;
            case 'h':
                timeMilliSec = timeInt * 60 * 60 * 1000;
                break;
            case 'd':
                timeMilliSec = timeInt * 60 * 60 * 24 * 1000;
                break;
            case 'w':
                timeMilliSec = timeInt * 60 * 60 * 24 * 7 * 1000;
                break;
            default:
                break;
        }
        return timeMilliSec;
    }

    /**
     * Returns the TimedActivities object
     *
     * @return The TimedActivities object used by this plugin
     */
    @NotNull
    public TimedActivities getTimedActivities() { return timedActivities; }

    /**
     * Returns the time zone.
     * <p>
     * The time zone was either set explicitly by the admin in the config file, or was determined automatically by
     * the plugin.
     *
     * @return The time zone the plugin is using for the displayed times
     * @see java.util.TimeZone
     */
    @NotNull
    public String getTimeZone() { return timeZone; }

    /**
     * Returns whether or not the config file allows users to reset their own stats
     *
     * @return Whether a user can reset their own stats
     */
    public boolean getResetOwnStats(){ return resetOwnStats; }

    /**
     * Returns the admin level required for a player to reset another player's stats
     *
     * @return Admin level necessary to reset another player's stats
     */
    @NotNull
    public String getResetAnotherPlayerStats() { return resetAnotherPlayerStats; }

    /**
     * Returns the admin level required for a player to reset the entire server's stats
     *
     * @return Admin level necessary to rest another player's stats
     */
    @NotNull
    public String getResetServerStats() { return resetServerStats; }

    /**
     * Returns whether death locations are being logged
     *
     * @return Whether or not death location stats are being saved
     */
    public boolean getDeath_locations() { return death_locations; }

    /**
     * Returns whether total time played is being logged
     *
     * @return Whether or not time played stats are being saved
     */
    public boolean getPlay_time() { return play_time; }

    /**
     * Returns whether last join time is being logged
     *
     * @return Whether or not last joined stats are being saved
     */
    public boolean getLast_join_time() { return last_join_time; }

    /**
     * Returns whether last leave time is being logged
     *
     * @return Whether or not last leave time stats are being saved
     */
    public boolean getLast_leave_time() { return last_leave_time; }

    /**
     * Returns whether specific words spoken is being logged
     *
     * @return Whether or not the specific words that player speak is being saved
     */
    public boolean getSpecific_words_spoken() { return specific_words_spoken; }

    /**
     * Returns whether words spoken is being logged
     *
     * @return Whether or not words spoken stats are being saved
     */
    public boolean getWords_spoken() { return words_spoken; }

    /**
     * Returns whether joins are being logged
     *
     * @return Whether or not number of joins stats are being saved
     */
    public boolean getJoins() { return joins; }

    /**
     * Returns whether the plugin should save the stats to disk in real-time
     *
     * @return Whether or not the plugin should write stats to disk in real-time
     */
    public boolean getSaveStatsRealTime() { return saveStatsRealTime; }

    /**
     * Returns whether mined ores is being logged
     *
     * @return Whether or not mined ores stats are being saved
     */
    public boolean getMined_ores() { return mined_ores; }

    /**
     * Returns the total backups the plugin should keep
     *
     * @return The total backups the plugin should keep
     */
    public int getBackupStatsNumber() { return backupStatsNumber; }

    /**
     * Return the current backup the plugin is on
     *
     * @return The current backup the plugin is on
     */
    public int getBackupNumber() { return backupNumber; }

    /**
     * Returns the backup stats location
     *
     * @return The location on the disk where the plugin should save the backups
     */
    public String getBackupStatsLocation() { return backupStatsLocation; }

    /**
     * Increase the backup number by one
     * <p>
     * This includes increase the backupNumber variable, and incrementing the value held on the disk by one as well
     */
    public void incrementBackupNumber() {
        backupNumber++;
        PrintWriter out = null;
        try {
            out = new PrintWriter(getDataFolder().getPath() + "/backup-number");
            out.println(backupNumber);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * Returns the naming scheme used to name backups
     *
     * @return The naming scheme used to name backups
     *
     * @see java.text.SimpleDateFormat
     */
    @NotNull
    public String getBackupName() { return backupName; }

    /**
     * Returns a list of the backups currently on the disk, but only those that the plugin knows of.
     * <p>
     * To prevent the deletion of other files or folders that the user does not mean to get deleted, the plugin will
     * keep it's own list of backups, and attempt ot make sure it stays correct. The plugin will only delete old backups
     * as it sees need from it's own internal list.
     *
     * @return A list of the backups currently on the disk
     */
    @NotNull
    public ArrayList<String> getBackups() { return backups; }

    /**
     * Returns the last time a player was on fire
     *
     * @param name The name of the player that was on fire
     * @return The time that the player was last on fire
     */
    public int getLastFireTime(String name) {
        Integer time = lastFireTime.get(name);
        return time == null ? 0 : time;
    }

    /**
     * Returns the last time a player was drowning
     *
     * @param name The name of the player that was drowning
     * @return The time that the player was last drowning
     */
    public int getLastDrownTime(String name) {
        Integer time = lastDrownTime.get(name);
        return time == null ? 0 : time;
    }

    /**
     * Returns the last time a player was poisoned
     *
     * @param name The name of the player that was poisoned
     * @return The time that the player was last poisoned
     */
    public int getLastPoisonTime(String name) {
        Integer time = lastPoisonTime.get(name);
        return time == null ? 0 : time;
    }

    /**
     * Sets the last time a player is on fire
     *
     * @param name The name of the player that is on fire
     * @param time The time the player was on fire
     */
    public void setLastFireTime(String name, int time) { lastFireTime.put(name, time); }

    /**
     * Sets the last time a player is drowning
     *
     * @param name The name of the player that is drowning
     * @param time The time the player was drowning
     */
    public void setLastDrowningTime(String name, int time) { lastDrownTime.put(name, time); }

    /**
     * Sets the last time a player is poisoned
     *
     * @param name The name of the player that is poisoned
     * @param time The time the player was poisoned
     */
    public void setLastPoisonTime(String name, int time) { lastPoisonTime.put(name, time); }

    /**
     * Returns whether the plugin should announce if someone is on fire
     *
     * @return Whether or not the plugin should announce when someone catches on fire
     */
    public boolean getOn_fire_announce() { return on_fire_announce; }

    /**
     * Returns whether the plugin should announce if someone is drowning
     *
     * @return Whether or not the plugin should announce when someone starts to drown
     */
    public boolean getDrowning_announce() { return drowning_announce; }

    /**
     * Returns whether the plugin should announce if someone is poisoned
     *
     * @return Whether or not the plugin should announce when someone gets poisoned
     */
    public boolean getPoison_announce() { return poison_announce; }

    /**
     * Recusrively deletes a folder, even if it's full
     *
     * @param f The directory that needs to be deleted
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public static void deleteFolder(File f) {
        if (f.isDirectory())
            if (f.listFiles() != null)
                for (File c : f.listFiles())
                    deleteFolder(c);
        f.delete();
    }

    @NotNull
    public File getStatsDir() { return statsDir; }

    @NotNull
    public File getPlayersFile() { return playersFile; }

    /**
     * Take the HashMap generated from the json using Gson and add to the UniqueHashMap of the player names and their
     * UUIDs. If a ValueNotUniqueException is thrown, nothing will be done, as this should absolutely
     * never happen, and if it does on the next players.json write it will be fixed.
     *
     * @param t the HashMap from the players.json
     */
    private void createPlayersUniqueMap(HashMap<String, UUID> t) {
        players = UniqueHashMap.create(String.class, UUID.class);
        if (t != null) {
            for (Map.Entry<String, UUID> pair : t.entrySet()) {
                try {
                    players.put(pair.getKey(), pair.getValue());
                } catch (ValueNotUniqueException e) {
                    getLogger().warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Read the players.json file and import it into a HashMap&gt;String, UUID&lt;, then call
     * {@link #createPlayersUniqueMap(java.util.HashMap) createPlayersUniqueHashMap()} with the HashMap&gt;String, UUID&lt;
     * created from the players.json file to initialize {@link #players players} with the already saved nickname-UUID
     * mappings.
     */
    private void readPlayersFile() {
        // See if the players file exists
        File playersFile = new File(getDataFolder(), "players.json");
        if (playersFile.exists()) {
            String json;
            try {
                json = StatCraft.readFile(playersFile.getPath(), StandardCharsets.UTF_8);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type tokenType = new TypeToken<HashMap<String, UUID>>(){}.getType();
                HashMap<String, UUID> tempPlayers = gson.fromJson(json, tokenType);
                createPlayersUniqueMap(tempPlayers);
            } catch (IOException e) {
                getLogger().severe("Fatal: Cannot parse players.json, perhaps a permission issue?");
                e.printStackTrace();
                enabled = false;
            }
        } else {
            players = UniqueHashMap.create(String.class, UUID.class);
        }
    }

    /**
     * Write to players.json the nickname-UUID mappings from {@link #players players} and check to make sure the stat
     * directories are up-to-date, as in make sure there is no known nickname-UUID pair that is still using the nickname
     * as the stat directory.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public void writePlayersFile() {

        // make sure there are files in the stats directory to look up
        if (statsDir.listFiles() != null)
        for (File f : statsDir.listFiles()) {
            // try and form a UUID object from the string
            // if this fails, we know the stat directory is not a UUID
            // add every directory name (except totals) to the list
            if (!f.getName().equals("totals"))
            try {
                UUID.fromString(f.getName());
            } catch (IllegalArgumentException e) {
                String newDirName = getServer().getOfflinePlayer(f.getName()).getUniqueId().toString();
                try {
                    Files.move(f.toPath(), new File(statsDir, newDirName).toPath());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (!players.containsValue(UUID.fromString(newDirName))) {
                    players.put(f.getName(), UUID.fromString(newDirName));
                } else if (
                        !players.containsKey(f.getName()) ||
                                !players.getKeyFromValue(UUID.fromString(newDirName)).equals(f.getName())) {
                    players.removeValue(UUID.fromString(newDirName));
                    players.put(f.getName(), newDirName);
                }
            }
        }

        // now that the stat directories have been updated, write the players.json file with the new nickname-UUID mappings.
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // we will use the PrintWriter to write out the json text
        if (players != null)
        try (PrintWriter pw = new PrintWriter(playersFile)) {
            // get the json from the UniqueHashMap (by simply using the keymap) and write it to the players file
            pw.println(gson.toJson(players.getKeyMap()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns {@link #threadLock threadLock} for synchronized blocks.
     *
     * @return {@link #threadLock threadLock} for synchronized blocks
     */
    public Object getThreadLock() { return threadLock; }
}