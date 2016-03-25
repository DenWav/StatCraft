/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.demonwav.statcraft.commands.BaseCommand;
import com.demonwav.statcraft.commands.sc.SCArrowsShot;
import com.demonwav.statcraft.commands.sc.SCBlocksBroken;
import com.demonwav.statcraft.commands.sc.SCBlocksPlaced;
import com.demonwav.statcraft.commands.sc.SCBucketsEmptied;
import com.demonwav.statcraft.commands.sc.SCBucketsFilled;
import com.demonwav.statcraft.commands.sc.SCDamageDealt;
import com.demonwav.statcraft.commands.sc.SCDamageTaken;
import com.demonwav.statcraft.commands.sc.SCDeaths;
import com.demonwav.statcraft.commands.sc.SCEggsThrown;
import com.demonwav.statcraft.commands.sc.SCEnderPearls;
import com.demonwav.statcraft.commands.sc.SCFirstJoin;
import com.demonwav.statcraft.commands.sc.SCFishCaught;
import com.demonwav.statcraft.commands.sc.SCHighestLevel;
import com.demonwav.statcraft.commands.sc.SCItemsCrafted;
import com.demonwav.statcraft.commands.sc.SCItemsDropped;
import com.demonwav.statcraft.commands.sc.SCItemsPickedUp;
import com.demonwav.statcraft.commands.sc.SCJoins;
import com.demonwav.statcraft.commands.sc.SCJumps;
import com.demonwav.statcraft.commands.sc.SCKicks;
import com.demonwav.statcraft.commands.sc.SCKills;
import com.demonwav.statcraft.commands.sc.SCLastSeen;
import com.demonwav.statcraft.commands.sc.SCLastSlept;
import com.demonwav.statcraft.commands.sc.SCMessagesSpoken;
import com.demonwav.statcraft.commands.sc.SCMined;
import com.demonwav.statcraft.commands.sc.SCMove;
import com.demonwav.statcraft.commands.sc.SCOnFire;
import com.demonwav.statcraft.commands.sc.SCPlayTime;
import com.demonwav.statcraft.commands.sc.SCReset;
import com.demonwav.statcraft.commands.sc.SCSnowballs;
import com.demonwav.statcraft.commands.sc.SCTabCompletes;
import com.demonwav.statcraft.commands.sc.SCTimeSlept;
import com.demonwav.statcraft.commands.sc.SCToolsBroken;
import com.demonwav.statcraft.commands.sc.SCWordsSpoken;
import com.demonwav.statcraft.commands.sc.SCWorldChanges;
import com.demonwav.statcraft.commands.sc.SCXpGained;
import com.demonwav.statcraft.config.ColorConfig;
import com.demonwav.statcraft.config.Config;
import com.demonwav.statcraft.config.com.md_5.config.FileYamlStorage;
import com.demonwav.statcraft.listeners.ArrowsShotListener;
import com.demonwav.statcraft.listeners.BlockListener;
import com.demonwav.statcraft.listeners.BucketEmptyListener;
import com.demonwav.statcraft.listeners.BucketFillListener;
import com.demonwav.statcraft.listeners.DamageDealtListener;
import com.demonwav.statcraft.listeners.DamageTakenListener;
import com.demonwav.statcraft.listeners.DeathListener;
import com.demonwav.statcraft.listeners.EggListener;
import com.demonwav.statcraft.listeners.EnderPearlListener;
import com.demonwav.statcraft.listeners.FishCaughtListener;
import com.demonwav.statcraft.listeners.HighestLevelListener;
import com.demonwav.statcraft.listeners.ItemDropListener;
import com.demonwav.statcraft.listeners.ItemPickUpListener;
import com.demonwav.statcraft.listeners.ItemsCraftedListener;
import com.demonwav.statcraft.listeners.JumpListener;
import com.demonwav.statcraft.listeners.KickListener;
import com.demonwav.statcraft.listeners.KillListener;
import com.demonwav.statcraft.listeners.OnFireListener;
import com.demonwav.statcraft.listeners.PlayTimeListener;
import com.demonwav.statcraft.listeners.SleepyTimeListener;
import com.demonwav.statcraft.listeners.SnowballListener;
import com.demonwav.statcraft.listeners.TabCompleteListener;
import com.demonwav.statcraft.listeners.ToolsBrokenListener;
import com.demonwav.statcraft.listeners.WordsSpokenListener;
import com.demonwav.statcraft.listeners.WorldChangeListener;
import com.demonwav.statcraft.listeners.XpGainedListener;
import com.demonwav.statcraft.querydsl.Players;
import com.demonwav.statcraft.querydsl.QPlayTime;
import com.demonwav.statcraft.querydsl.QPlayers;
import com.demonwav.statcraft.querydsl.QSeen;
import com.demonwav.statcraft.querydsl.QSleep;
import com.demonwav.statcraft.sql.DatabaseManager;
import com.demonwav.statcraft.sql.ThreadManager;
import com.mysema.query.QueryException;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatCraft extends JavaPlugin {

    private DatabaseManager databaseManager;

    private HashMap<UUID, Integer> lastFireTime = new HashMap<>();
    private HashMap<UUID, Integer> lastDrownTime = new HashMap<>();
    private HashMap<UUID, Integer> lastPoisonTime = new HashMap<>();
    private HashMap<UUID, Integer> lastWitherTime = new HashMap<>();

    private ThreadManager threadManager = new ThreadManager(this);

    // Base Command
    private BaseCommand baseCommand = new BaseCommand(this);

    private String timeZone;
    public ConcurrentHashMap<String, UUID> players = new ConcurrentHashMap<>();

    // So we know if it's fully been enabled or not
    private AtomicBoolean enabler = new AtomicBoolean(false);

    // Config
    private Config config;
    final private FileYamlStorage<Config> configStorage = new FileYamlStorage<>(new File(getDataFolder(), "config.yml"), Config.class, this);

    public Config config() {
        return config;
    }

    @Override
    final public void onEnable() {
        config = configStorage.load();
        verifyConfigColors();
        configStorage.save();

        databaseManager = new DatabaseManager(this);
        if (isEnabled()) {
            databaseManager.setupDatabase();
            if (!isEnabled())
                return;

            // set the time zone of the server
            if (config().getTimezone().equalsIgnoreCase("auto")) {
                TimeZone tz = Calendar.getInstance().getTimeZone();
                timeZone = tz.getDisplayName(false, TimeZone.SHORT);
            } else {
                timeZone = config().getTimezone();
            }

            // Register timers
            getServer().getScheduler().runTaskTimer(this, threadManager, 1, 1);

            getCommand("sc").setExecutor(baseCommand);

            /* ****************************************************** */
            /* To protect against NoClassDefFoundError in onDisable() */
            /* */ QSeen.seen.getClass();                           /* */
            /* */ QPlayTime.playTime.getClass();                   /* */
            /* */ QSleep.sleep.getClass();                         /* */
            /* ****************************************************** */

            createListeners();
            initializePlaytimeAndBed();
            setupPlayers();
        }
    }

    private void setupPlayers() {
        List<Players> players = null;
        try (final Connection connection = getDatabaseManager().getConnection()) {
            players = getDatabaseManager().getNewQuery(connection).from(QPlayers.players).list(QPlayers.players);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (players != null) {
            for (Players player : players) {
                this.players.put(player.getName(), Util.byteToUUID(player.getUuid()));
            }
        }
    }

    private void initializePlaytimeAndBed() {
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                // Insert game join / bed enter data
                final int id;
                try (final Connection connection = getDatabaseManager().getConnection()) {
                    id = setupPlayer(player, connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                getThreadManager().scheduleRaw(
                    QSeen.class, (connection) -> Util.runQuery(
                            QSeen.class,
                            (s, clause) ->
                                clause.columns(s.id, s.lastJoinTime).values(id, currentTime).execute(),
                            (s, clause) ->
                                clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute(),
                            connection, this
                        )
                );

                if (player.isSleeping()) {
                    getThreadManager().scheduleRaw(
                        QSleep.class, (connection) -> Util.runQuery(
                           QSleep.class, (s, clause) ->
                                clause.columns(s.id, s.enterBed).values(id, currentTime).execute(),
                            (s, clause) ->
                                clause.where(s.id.eq(id)).set(s.enterBed, currentTime).execute(),
                            connection, this
                        )
                    );
                }
            });
            enabler.set(true);
        });
    }

    private void finishPlaytimeAndBed() {
        final int currentTime = (int)(System.currentTimeMillis() / 1000L);

        // We can't start an async task here, so just do it on the main thread
        getServer().getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            int id = getDatabaseManager().getPlayerId(uuid);

            try (final Connection connection = getDatabaseManager().getConnection()) {
                Util.runQuery(QSeen.class, (s, clause) ->
                        clause.columns(s.id, s.lastLeaveTime).values(id, currentTime).execute(),
                    (s, clause) ->
                        clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute(),
                    connection, this
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }

            final int currentPlayTime = (int) Math.round(player.getStatistic(Statistic.PLAY_ONE_TICK) * 0.052);

            try (final Connection connection = getDatabaseManager().getConnection()) {
                Util.runQuery(QPlayTime.class, (p, clause) ->
                        clause.columns(p.id, p.amount).values(id, currentPlayTime).execute(),
                    (p, clause) ->
                        clause.where(p.id.eq(id)).set(p.amount, currentPlayTime).execute(),
                    connection, this
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }


            if (player.isSleeping()) {
                try (final Connection connection = getDatabaseManager().getConnection()) {
                    Util.runQuery(QSleep.class, (s, query) -> {
                            Map<String, Integer> map = new HashMap<>();

                            Integer enterBed = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed);
                            enterBed = enterBed == null ? 0 : enterBed;
                            if (enterBed != 0) {
                                map.put("timeSlept", currentTime - enterBed);
                            } else {
                                map.put("timeSlept", 0);
                            }

                            return map;
                        }, (s, clause, map) -> {
                            if (map.get("timeSlept") == 0) {
                                clause.columns(s.id, s.leaveBed).values(id, currentTime).execute();
                            } else {
                                clause.columns(s.id, s.leaveBed, s.timeSlept)
                                    .values(id, currentTime, map.get("timeSlept")).execute();
                            }
                        }, (s, clause, map) -> {
                            if (map.get("timeSlept") == 0) {
                                clause.where(s.id.eq(id)).set(s.leaveBed, currentTime).execute();
                            } else {
                                clause.where(s.id.eq(id))
                                    .set(s.leaveBed, currentPlayTime)
                                    .set(s.timeSlept, map.get("timeSlept"))
                                    .execute();
                            }
                        },
                        connection, this
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void verifyConfigColors() {
        ColorConfig defaultConfig = new ColorConfig();

        for (Field field : config().getColors().getClass().getFields()) {
            try {
                String s = ((String)field.get(config().getColors())).trim().toUpperCase().replaceAll("\\s+", "_");
                field.set(config().getColors(), s);
                ChatColor.valueOf(s);
            } catch (Exception e) {
                try {
                    getLogger().warning("The color value '" + field.get(config().getColors()) + "' specified for colors." +
                            field.getName() + " is invalid, resetting to the default value of " + field.get(defaultConfig));
                    field.set(config().getColors(), field.get(defaultConfig));
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void createListeners() {
        StringBuilder statsEnabled = new StringBuilder();
        // load up the listeners
        if (config.getStats().isDeaths()) {
            getServer().getPluginManager().registerEvents(new DeathListener(this), this);
            statsEnabled.append(" death");
            new SCDeaths(this);
        }

        if (config.getStats().isBlocks()) {
            getServer().getPluginManager().registerEvents(new BlockListener(this), this);
            new SCBlocksBroken(this);
            new SCBlocksPlaced(this);
            statsEnabled.append(" block");

            if (config.getStats().isSpecificBlocks()) {
                statsEnabled.append(" mined");
                new SCMined(this);
            }
        }

        if (config.getStats().isPlayTime() || config.getStats().isFirstJoinTime()) {
            getServer().getPluginManager().registerEvents(new PlayTimeListener(this), this);
            if (config.getStats().isLastSeen()) {
                statsEnabled.append(" last_seen");
                new SCLastSeen(this);
            }
            if (config.getStats().isPlayTime()) {
                statsEnabled.append(" playtime");
                new SCPlayTime(this);
            }
            if (config.getStats().isJoins()) {
                statsEnabled.append(" joins");
                new SCJoins(this);
            }
            if (config.getStats().isFirstJoinTime()) {
                statsEnabled.append(" first_join_time");
                new SCFirstJoin(this);
            }
        }

        if (config.getStats().isItemPickUps()) {
            getServer().getPluginManager().registerEvents(new ItemPickUpListener(this), this);
            statsEnabled.append(" item_pickups");
            new SCItemsPickedUp(this);
        }

        if (config.getStats().isItemDrops()) {
            getServer().getPluginManager().registerEvents(new ItemDropListener(this), this);
            statsEnabled.append(" item_drops");
            new SCItemsDropped(this);
        }

        if (config.getStats().isItemsCrafted()) {
            getServer().getPluginManager().registerEvents(new ItemsCraftedListener(this), this);
            statsEnabled.append(" items_crafted");
            new SCItemsCrafted(this);
        }

        if (config.getStats().isOnFire()) {
            getServer().getPluginManager().registerEvents(new OnFireListener(this), this);
            statsEnabled.append(" on_fire");
            new SCOnFire(this);
        }

        if (config.getStats().isToolsBroken()) {
            getServer().getPluginManager().registerEvents(new ToolsBrokenListener(this), this);
            statsEnabled.append(" tools_broken");
            new SCToolsBroken(this);
        }

        if (config.getStats().isArrowsShot()) {
            getServer().getPluginManager().registerEvents(new ArrowsShotListener(this), this);
            statsEnabled.append(" arrows_shot");
            new SCArrowsShot(this);
        }

        if (config.getStats().isBucketsEmptied()) {
            getServer().getPluginManager().registerEvents(new BucketFillListener(this), this);
            statsEnabled.append(" bucket_fill");
            new SCBucketsFilled(this);
        }

        if (config.getStats().isBucketsEmptied()) {
            getServer().getPluginManager().registerEvents(new BucketEmptyListener(this), this);
            statsEnabled.append(" bucket_empty");
            new SCBucketsEmptied(this);
        }

        if (config.getStats().isBed()) {
            getServer().getPluginManager().registerEvents(new SleepyTimeListener(this), this);
            statsEnabled.append(" bed");
            new SCTimeSlept(this);
            new SCLastSlept(this);
        }

        if (config.getStats().isWorldChanges()) {
            getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
            statsEnabled.append(" world_change");
            new SCWorldChanges(this);
        }

        if (config.getStats().isMessagesSpoken()) {
            getServer().getPluginManager().registerEvents(new WordsSpokenListener(this), this);
            statsEnabled.append(" message_spoken");
            new SCMessagesSpoken(this);
            if (config.getStats().isWordsSpoken())
                new SCWordsSpoken(this);
        }

        if (config.getStats().isDamageTaken()) {
            getServer().getPluginManager().registerEvents(new DamageTakenListener(this), this);
            statsEnabled.append(" damage_taken");
            new SCDamageTaken(this);
        }

        if (config.getStats().isDamageDealt()) {
            getServer().getPluginManager().registerEvents(new DamageDealtListener(this), this);
            statsEnabled.append(" damage_dealt");
            new SCDamageDealt(this);
        }

        if (config.getStats().isFishCaught()) {
            getServer().getPluginManager().registerEvents(new FishCaughtListener(this), this);
            statsEnabled.append(" fish_caught");
            new SCFishCaught(this);
        }

        if (config.getStats().isXpGained()) {
            getServer().getPluginManager().registerEvents(new XpGainedListener(this), this);
            statsEnabled.append(" xp_gained");
            new SCXpGained(this);
        }

        if (config.getStats().isKills()) {
            getServer().getPluginManager().registerEvents(new KillListener(this), this);
            statsEnabled.append(" kills");
            new SCKills(this);
        }

        if (config.getStats().isHighestLevel()) {
            getServer().getPluginManager().registerEvents(new HighestLevelListener(this), this);
            statsEnabled.append(" highest_level");
            new SCHighestLevel(this);
        }

        if (config.getStats().isTabCompletes()) {
            getServer().getPluginManager().registerEvents(new TabCompleteListener(this), this);
            statsEnabled.append(" tab_complete");
            new SCTabCompletes(this);
        }

        if (config.getStats().isEggsThrown()) {
            getServer().getPluginManager().registerEvents(new EggListener(this), this);
            statsEnabled.append(" eggs_thrown");
            new SCEggsThrown(this);
        }

        if (config.getStats().isEnderPearls()) {
            getServer().getPluginManager().registerEvents(new EnderPearlListener(this), this);
            statsEnabled.append(" ender_pearls");
            new SCEnderPearls(this);
        }

        if (config.getStats().isSnowBalls()) {
            getServer().getPluginManager().registerEvents(new SnowballListener(this), this);
            statsEnabled.append(" snow_balls");
            new SCSnowballs(this);
        }

        if (config.getStats().isMove()) {
            // Every 2 seconds
            getServer().getScheduler().runTaskTimer(this, new ServerStatUpdater.Move(this), 1, 40);
            statsEnabled.append(" move");
            new SCMove(this);
        }

        if (config.getStats().isJumps()) {
            // Every 10 seconds
            getServer().getPluginManager().registerEvents(new JumpListener(this), this);
            statsEnabled.append(" jumps");
            new SCJumps(this);
        }

//        if (config.stats.animals_bred) {
//            getServer().getPluginManager().registerEvents(new AnimalsBredListener(this), this);
//            statsEnabled.append(" animals_bred");
//        }

        if (config.getStats().isKicks()) {
            getServer().getPluginManager().registerEvents(new KickListener(this), this);
            statsEnabled.append(" kicks");
            new SCKicks(this);
        }

        getLogger().info("Successfully enabled:" + statsEnabled);

        new SCReset(this);
    }

    @Override
    final public void onDisable() {
        if (enabler.get())
            finishPlaytimeAndBed();

        if (threadManager != null)
            threadManager.stop();

        if (getDatabaseManager() != null)
            getDatabaseManager().close();

        getServer().getScheduler().cancelTasks(this);
     }

    public int setupPlayer(OfflinePlayer player, Connection connection) {
        byte[] array = Util.UUIDToByte(player.getUniqueId());
        String name = player.getName();
        // Check player / id listing
        QPlayers p = QPlayers.players;
        SQLQuery query = getDatabaseManager().getNewQuery(connection);

        if (query == null)
            return -1;

        Players result = query.from(p).where(p.uuid.eq(array)).uniqueResult(p);

        if (result == null) {
            SQLUpdateClause update = getDatabaseManager().getUpdateClause(connection, p);
            // Blank out any conflicting names
            update
                .where(p.name.eq(name))
                .set(p.name, "")
                .execute();
            SQLInsertClause insert = getDatabaseManager().getInsertClause(connection, p);
            // Insert new player listing
            insert
                .columns(p.uuid, p.name)
                .values(array, name)
                .execute();

            checkBlanks();
        } else if (!result.getName().equals(name)) {
            SQLUpdateClause update = getDatabaseManager().getUpdateClause(connection, p);
            // Blank out any conflicting names
            update
                .where(p.name.eq(name))
                .set(p.name, "")
                .execute();
            // Change name of UUID player
            update
                .where(p.uuid.eq(array))
                .set(p.name, name)
                .execute();

            checkBlanks();
        }

        int id = getDatabaseManager().getPlayerId(player.getUniqueId());

        if (config.getStats().isFirstJoinTime()) {
            query = getDatabaseManager().getNewQuery(connection);
            QSeen s = QSeen.seen;
            Integer time = query.from(s).where(s.id.eq(id)).uniqueResult(s.firstJoinTime);
            time = time == null ? 0 : time;
            if (time != (int)(player.getFirstPlayed() / 1000L)) {
                try {
                    SQLInsertClause clause = getDatabaseManager().getInsertClause(connection, s);
                    clause.columns(s.id, s.firstJoinTime).values(id, (int) (player.getFirstPlayed() / 1000L)).execute();
                } catch (QueryException e) {
                    SQLUpdateClause clause = getDatabaseManager().getUpdateClause(connection, s);
                    clause.where(s.id.eq(id)).set(s.firstJoinTime, (int)(player.getFirstPlayed() / 1000L)).execute();
                }
            }
        }

        if (player.isOnline()) {
            final int currentPlayTime = (int) Math.round(((Player) player).getStatistic(Statistic.PLAY_ONE_TICK) * 0.052);

            Util.runQuery(QPlayTime.class, (pl, clause) ->
                    clause.columns(pl.id, pl.amount).values(id, currentPlayTime).execute(),
                (pl, clause) ->
                    clause.where(pl.id.eq(id)).set(pl.amount, currentPlayTime).execute(),
                connection, this
            );
        }

        return id;
    }

    /**
     * Returns the time zone.
     * <p>
     * The time zone was either set explicitly by the admin in the config file, or was determined automatically by
     * the plugin.
     *
     * @return The time zone the plugin is using for the displayed times
     * @see java.util.TimeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Returns the last time a player was on fire
     *
     * @param uuid The uuid of the player that was on fire
     * @return The time that the player was last on fire
     */
    public int getLastFireTime(UUID uuid) {
        Integer time = lastFireTime.get(uuid);
        return time == null ? 0 : time;
    }

    /**
     * Returns the last time a player was drowning
     *
     * @param uuid The uuid of the player that was drowning
     * @return The time that the player was last drowning
     */
    public int getLastDrownTime(UUID uuid) {
        Integer time = lastDrownTime.get(uuid);
        return time == null ? 0 : time;
    }

    /**
     * Returns the last time a player was poisoned
     *
     * @param uuid The uuid of the player that was poisoned
     * @return The time that the player was last poisoned
     */
    public int getLastPoisonTime(UUID uuid) {
        Integer time = lastPoisonTime.get(uuid);
        return time == null ? 0 : time;
    }

    /**
     * Returns the last time a player was withering away
     *
     * @param uuid The uuid of the player that was withering away
     * @return The time that the player was last withering away
     */
    public int getLastWitherTime(UUID uuid) {
        Integer time = lastWitherTime.get(uuid);
        return time == null ? 0 : time;
    }

    /**
     * Sets the last time a player is on fire
     *
     * @param uuid The uuid of the player that is on fire
     * @param time The time the player was on fire
     */
    public void setLastFireTime(UUID uuid, int time) {
        lastFireTime.put(uuid, time);
    }

    /**
     * Sets the last time a player is drowning
     *
     * @param uuid The uuid of the player that is drowning
     * @param time The time the player was drowning
     */
    public void setLastDrowningTime(UUID uuid, int time) {
        lastDrownTime.put(uuid, time);
    }

    /**
     * Sets the last time a player is poisoned
     *
     * @param uuid The uuid of the player that is poisoned
     * @param time The time the player was poisoned
     */
    public void setLastPoisonTime(UUID uuid, int time) {
        lastPoisonTime.put(uuid, time);
    }

    /**
     * Sets the last time a player is withering away
     *
     * @param uuid The uuid of the player that is withering awawy
     * @param time The time the player is withering away
     */
    public void setLastWitherTime(UUID uuid, int time) {
        lastWitherTime.put(uuid, time);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BaseCommand getBaseCommand() {
        return baseCommand;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    private static String getCurrentName(UUID uuid) {
        // Get JSON from Mojang API
        final String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replaceAll("-", "") + "/names";
        HttpURLConnection conn;
        BufferedReader rd = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Parse the JSON, checking for errors as we go
        final String s = sb.toString();
        if (s.isEmpty()) {
            throw new IllegalStateException("No response from Mojang API.");
        }
        Object list = JSONValue.parse(s);
        if (!(list instanceof JSONArray))
            throw new IllegalStateException("Mojang API returned incorrect JSON: " + s);
        JSONArray names = (JSONArray) list;

        Object last = names.get(names.size() - 1);
        if (!(last instanceof JSONObject))
            throw new IllegalStateException("Mojang API returned incorrect JSON: " + s);

        JSONObject name = (JSONObject) last;
        if (name.isEmpty()) {
            throw new IllegalStateException("Mojang API returned bad name object: " + s);
        }

        Object fin = name.get("name");
        if (!(fin instanceof String)) {
            throw new IllegalStateException("Mojang API returned bad name object: " + s);
        }

        String n = (String) fin;
        if (n.isEmpty()) {
            throw new IllegalStateException("Mojang API returned bad name: " + s);
        }

        return n;
    }

    private void checkBlanks() {
        getThreadManager().scheduleRaw(QPlayers.class, (connection) -> {
            QPlayers p = QPlayers.players;
            SQLQuery query = getDatabaseManager().getNewQuery(connection);

            if (query == null)
                return;

            List<Players> result = query.from(p).where(p.name.eq("")).list(p);
            for (Players players : result) {
                UUID uuid = Util.byteToUUID(players.getUuid());
                String name;
                try {
                    name = getCurrentName(uuid);

                    SQLUpdateClause update = getDatabaseManager().getUpdateClause(connection, p);
                    update
                        .where(p.uuid.eq(players.getUuid()))
                        .set(p.name, name)
                        .execute();
                } catch (Exception e) {
                    getServer().getLogger().warning("Was unable to set new name for " + uuid.toString());
                    e.printStackTrace();
                }
            }
        });
    }
}
