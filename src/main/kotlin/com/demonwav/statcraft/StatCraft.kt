/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft

import com.demonwav.statcraft.commands.BaseCommand
import com.demonwav.statcraft.commands.sc.SCArrowsShot
import com.demonwav.statcraft.commands.sc.SCBlocksBroken
import com.demonwav.statcraft.commands.sc.SCBlocksPlaced
import com.demonwav.statcraft.commands.sc.SCBucketsEmptied
import com.demonwav.statcraft.commands.sc.SCBucketsFilled
import com.demonwav.statcraft.commands.sc.SCDamageDealt
import com.demonwav.statcraft.commands.sc.SCDamageTaken
import com.demonwav.statcraft.commands.sc.SCDeaths
import com.demonwav.statcraft.commands.sc.SCEggsThrown
import com.demonwav.statcraft.commands.sc.SCEnderPearls
import com.demonwav.statcraft.commands.sc.SCFirstJoin
import com.demonwav.statcraft.commands.sc.SCFishCaught
import com.demonwav.statcraft.commands.sc.SCHighestLevel
import com.demonwav.statcraft.commands.sc.SCItemsCrafted
import com.demonwav.statcraft.commands.sc.SCItemsDropped
import com.demonwav.statcraft.commands.sc.SCItemsPickedUp
import com.demonwav.statcraft.commands.sc.SCJoins
import com.demonwav.statcraft.commands.sc.SCJumps
import com.demonwav.statcraft.commands.sc.SCKicks
import com.demonwav.statcraft.commands.sc.SCKills
import com.demonwav.statcraft.commands.sc.SCLastSeen
import com.demonwav.statcraft.commands.sc.SCLastSlept
import com.demonwav.statcraft.commands.sc.SCMessagesSpoken
import com.demonwav.statcraft.commands.sc.SCMined
import com.demonwav.statcraft.commands.sc.SCMove
import com.demonwav.statcraft.commands.sc.SCOnFire
import com.demonwav.statcraft.commands.sc.SCPlayTime
import com.demonwav.statcraft.commands.sc.SCReset
import com.demonwav.statcraft.commands.sc.SCSnowballs
import com.demonwav.statcraft.commands.sc.SCTabCompletes
import com.demonwav.statcraft.commands.sc.SCTimeSlept
import com.demonwav.statcraft.commands.sc.SCToolsBroken
import com.demonwav.statcraft.commands.sc.SCWordsSpoken
import com.demonwav.statcraft.commands.sc.SCWorldChanges
import com.demonwav.statcraft.commands.sc.SCXpGained
import com.demonwav.statcraft.config.ColorConfig
import com.demonwav.statcraft.config.Config
import com.demonwav.statcraft.listeners.ArrowsShotListener
import com.demonwav.statcraft.listeners.BlockListener
import com.demonwav.statcraft.listeners.BucketEmptyListener
import com.demonwav.statcraft.listeners.BucketFillListener
import com.demonwav.statcraft.listeners.DamageDealtListener
import com.demonwav.statcraft.listeners.DamageTakenListener
import com.demonwav.statcraft.listeners.DeathListener
import com.demonwav.statcraft.listeners.EggListener
import com.demonwav.statcraft.listeners.EnderPearlListener
import com.demonwav.statcraft.listeners.FishCaughtListener
import com.demonwav.statcraft.listeners.HighestLevelListener
import com.demonwav.statcraft.listeners.ItemDropListener
import com.demonwav.statcraft.listeners.ItemPickUpListener
import com.demonwav.statcraft.listeners.ItemsCraftedListener
import com.demonwav.statcraft.listeners.JumpListener
import com.demonwav.statcraft.listeners.KickListener
import com.demonwav.statcraft.listeners.KillListener
import com.demonwav.statcraft.listeners.OnFireListener
import com.demonwav.statcraft.listeners.PlayTimeListener
import com.demonwav.statcraft.listeners.SleepyTimeListener
import com.demonwav.statcraft.listeners.SnowballListener
import com.demonwav.statcraft.listeners.TabCompleteListener
import com.demonwav.statcraft.listeners.ToolsBrokenListener
import com.demonwav.statcraft.listeners.WordsSpokenListener
import com.demonwav.statcraft.listeners.WorldChangeListener
import com.demonwav.statcraft.listeners.XpGainedListener
import com.demonwav.statcraft.querydsl.Players
import com.demonwav.statcraft.querydsl.QPlayTime
import com.demonwav.statcraft.querydsl.QPlayers
import com.demonwav.statcraft.querydsl.QSeen
import com.demonwav.statcraft.querydsl.QSleep
import com.demonwav.statcraft.sql.DatabaseManager
import com.demonwav.statcraft.sql.ThreadManager
import com.md_5.config.FileYamlStorage
import com.mysema.query.QueryException
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Connection
import java.sql.SQLException
import java.util.Calendar
import java.util.HashMap
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class StatCraft : JavaPlugin() {

    lateinit var databaseManager: DatabaseManager

    val lastFireTime = ConcurrentHashMap<UUID, Int>()
    val lastDrownTime = ConcurrentHashMap<UUID, Int>()
    val lastPoisonTime = ConcurrentHashMap<UUID, Int>()
    val lastWitherTime = ConcurrentHashMap<UUID, Int>()

    val threadManager = ThreadManager(this)

    val baseCommand = BaseCommand(this)

    var timeZone: String? = Calendar.getInstance().timeZone.getDisplayName(false, TimeZone.SHORT)
    var players = ConcurrentHashMap<String, UUID>()
    var moveUpdater = ServerStatUpdater.Move(this)

    val enabler = AtomicBoolean(false)

    // Config stuff
    val configStorage = FileYamlStorage<Config>(File(dataFolder, "config.yml"), Config::class.java)
    lateinit var config: Config

    override fun onEnable() {
        config = configStorage.load()
        verifyConfigColors()
        configStorage.save()

        databaseManager = DatabaseManager(this)
        if (!isEnabled) {
            return
        }

        databaseManager.setupDatabase()
        if (!isEnabled) {
            return
        }

        if (!config.timezone.equals("auto", true)) {
            timeZone = config.timezone
        }

        server.scheduler.runTaskTimer(this, threadManager, 1, 1)

        getCommand("sc").executor = baseCommand

        /* ****************************************************** */
        /* To protect against NoClassDefFoundError in onDisable() */
        /* */ QSeen.seen.javaClass                             /* */
        /* */ QPlayTime.playTime.javaClass                     /* */
        /* */ QSleep.sleep.javaClass                           /* */
        /* ****************************************************** */

        createListeners()
        initializePlaytimeAndBed()
        setupPlayers()
    }

    private fun verifyConfigColors() {
        val defaultConfig = ColorConfig()

        for (field in config.colors.javaClass.fields) {
            try {
                val s = (field.get(config.colors) as String).trim { it <= ' ' }.toUpperCase().replace("\\s+".toRegex(), "_")
                field.set(config.colors, s)
                ChatColor.valueOf(s)
            } catch (e: Exception) {
                try {
                    logger.warning("The color value '${field.get(config.colors)}' specified for colors." +
                        "${field.name} is invalid, resetting to the default value of ${field.get(defaultConfig)}")
                    field.set(config.colors, field.get(defaultConfig))
                } catch (e1: IllegalAccessException) {
                    e1.printStackTrace()
                }
            }
        }
    }

    private fun createListeners() {
        val statsEnabled = StringBuilder()
        // load up the listeners
        if (config.stats.deaths) {
            server.pluginManager.registerEvents(DeathListener(this), this)
            statsEnabled.append(" death")
            SCDeaths(this)
        }

        if (config.stats.blocks) {
            server.pluginManager.registerEvents(BlockListener(this), this)
            SCBlocksBroken(this)
            SCBlocksPlaced(this)
            statsEnabled.append(" block")

            if (config.stats.specificBlocks) {
                statsEnabled.append(" mined")
                SCMined(this)
            }
        }

        if (config.stats.playTime || config.stats.firstJoinTime) {
            server.pluginManager.registerEvents(PlayTimeListener(this), this)
            if (config.stats.lastSeen) {
                statsEnabled.append(" last_seen")
                SCLastSeen(this)
            }
            if (config.stats.playTime) {
                statsEnabled.append(" playtime")
                SCPlayTime(this)
            }
            if (config.stats.joins) {
                statsEnabled.append(" joins")
                SCJoins(this)
            }
            if (config.stats.firstJoinTime) {
                statsEnabled.append(" first_join_time")
                SCFirstJoin(this)
            }
        }

        if (config.stats.itemPickUps) {
            server.pluginManager.registerEvents(ItemPickUpListener(this), this)
            statsEnabled.append(" item_pickups")
            SCItemsPickedUp(this)
        }

        if (config.stats.itemDrops) {
            server.pluginManager.registerEvents(ItemDropListener(this), this)
            statsEnabled.append(" item_drops")
            SCItemsDropped(this)
        }

        if (config.stats.itemsCrafted) {
            server.pluginManager.registerEvents(ItemsCraftedListener(this), this)
            statsEnabled.append(" items_crafted")
            SCItemsCrafted(this)
        }

        if (config.stats.onFire) {
            server.pluginManager.registerEvents(OnFireListener(this), this)
            statsEnabled.append(" on_fire")
            SCOnFire(this)
        }

        if (config.stats.toolsBroken) {
            server.pluginManager.registerEvents(ToolsBrokenListener(this), this)
            statsEnabled.append(" tools_broken")
            SCToolsBroken(this)
        }

        if (config.stats.arrowsShot) {
            server.pluginManager.registerEvents(ArrowsShotListener(this), this)
            statsEnabled.append(" arrows_shot")
            SCArrowsShot(this)
        }

        if (config.stats.bucketsFilled) {
            server.pluginManager.registerEvents(BucketFillListener(this), this)
            statsEnabled.append(" bucket_fill")
            SCBucketsFilled(this)
        }

        if (config.stats.bucketsEmptied) {
            server.pluginManager.registerEvents(BucketEmptyListener(this), this)
            statsEnabled.append(" bucket_empty")
            SCBucketsEmptied(this)
        }

        if (config.stats.bed) {
            server.pluginManager.registerEvents(SleepyTimeListener(this), this)
            statsEnabled.append(" bed")
            SCTimeSlept(this)
            SCLastSlept(this)
        }

        if (config.stats.worldChanges) {
            server.pluginManager.registerEvents(WorldChangeListener(this), this)
            statsEnabled.append(" world_change")
            SCWorldChanges(this)
        }

        if (config.stats.messagesSpoken) {
            server.pluginManager.registerEvents(WordsSpokenListener(this), this)
            statsEnabled.append(" message_spoken")
            SCMessagesSpoken(this)
            if (config.stats.wordsSpoken) {
                SCWordsSpoken(this)
            }
        }

        if (config.stats.damageTaken) {
            server.pluginManager.registerEvents(DamageTakenListener(this), this)
            statsEnabled.append(" damage_taken")
            SCDamageTaken(this)
        }

        if (config.stats.damageDealt) {
            server.pluginManager.registerEvents(DamageDealtListener(this), this)
            statsEnabled.append(" damage_dealt")
            SCDamageDealt(this)
        }

        if (config.stats.fishCaught) {
            server.pluginManager.registerEvents(FishCaughtListener(this), this)
            statsEnabled.append(" fish_caught")
            SCFishCaught(this)
        }

        if (config.stats.xpGained) {
            server.pluginManager.registerEvents(XpGainedListener(this), this)
            statsEnabled.append(" xp_gained")
            SCXpGained(this)
        }

        if (config.stats.kills) {
            server.pluginManager.registerEvents(KillListener(this), this)
            statsEnabled.append(" kills")
            SCKills(this)
        }

        if (config.stats.highestLevel) {
            server.pluginManager.registerEvents(HighestLevelListener(this), this)
            statsEnabled.append(" highest_level")
            SCHighestLevel(this)
        }

        if (config.stats.tabCompletes) {
            server.pluginManager.registerEvents(TabCompleteListener(this), this)
            statsEnabled.append(" tab_complete")
            SCTabCompletes(this)
        }

        if (config.stats.eggsThrown) {
            server.pluginManager.registerEvents(EggListener(this), this)
            statsEnabled.append(" eggs_thrown")
            SCEggsThrown(this)
        }

        if (config.stats.enderPearls) {
            server.pluginManager.registerEvents(EnderPearlListener(this), this)
            statsEnabled.append(" ender_pearls")
            SCEnderPearls(this)
        }

        if (config.stats.snowBalls) {
            server.pluginManager.registerEvents(SnowballListener(this), this)
            statsEnabled.append(" snow_balls")
            SCSnowballs(this)
        }

        if (config.stats.move) {
            // Every 2 seconds
            server.scheduler.runTaskTimer(this, moveUpdater, 1, 40)
            statsEnabled.append(" move")
            SCMove(this)
        }

        if (config.stats.jumps) {
            // Every 10 seconds
            server.pluginManager.registerEvents(JumpListener(this), this)
            statsEnabled.append(" jumps")
            SCJumps(this)
        }

//        if (config.stats.isAnimalsBred) {
//          server.pluginManager.registerEvents(AnimalsBredListener(this), this)
//          statsEnabled.append(" animals_bred")
//        }

        if (config.stats.kicks) {
            server.pluginManager.registerEvents(KickListener(this), this)
            statsEnabled.append(" kicks")
            SCKicks(this)
        }

        logger.info("Successfully enabled:" + statsEnabled)

        SCReset(this)
    }

    private fun initializePlaytimeAndBed() {
        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

        server.scheduler.runTaskAsynchronously(this) {
            server.onlinePlayers.forEach { player ->
                // Insert game join / bed enter data
                var id = 0
                try {
                    databaseManager.connection.use { id = setupPlayer(player, this) ?: return@forEach }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    return@forEach
                }

                threadManager.scheduleRaw(
                    QSeen::class.java, { connection ->
                        QSeen.seen.runQuery(
                            { s, clause -> clause.columns(s.id, s.lastJoinTime).values(id, currentTime).execute() },
                            { s, clause -> clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute() },
                            connection, this
                        )
                    }
                )

                if (player.isSleeping) {
                    threadManager.scheduleRaw(
                        QSleep::class.java, { connection ->
                            QSleep.sleep.runQuery(
                                { s, clause -> clause.columns(s.id, s.enterBed).values(id, currentTime).execute() },
                                { s, clause -> clause.where(s.id.eq(id)).set(s.enterBed, currentTime).execute() },
                                connection, this
                            )
                        }
                    )
                }
            }
            enabler.set(true)
        }
    }

    private fun setupPlayers() {
        var players: List<Players>? = null
        try {
            databaseManager.connection.use {
                val query = databaseManager.getNewQuery(this) ?: return@use
                players = query.from(QPlayers.players).list(QPlayers.players)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        if (players != null) {
            for (player in players!!) {
                this.players.put(player.name, player.uuid.toUUID())
            }
        }
    }

    override fun onDisable() {
        if (enabler.get()) {
            finishPlaytimeAndBed()

            threadManager.close()
            databaseManager.close()
        }

        server.scheduler.cancelTasks(this)
    }

    private fun finishPlaytimeAndBed() {
        val currentTime = (System.currentTimeMillis() / 1000L).toInt()

        val plugin = this
        // We can't start an async task here, so just do it on the main thread
        server.onlinePlayers.forEach { player ->
            val uuid = player.uniqueId
            val id = databaseManager.getPlayerId(uuid)

            try {
                databaseManager.connection.use {
                    QSeen.seen.runQuery(
                        { s, clause -> clause.columns(s.id, s.lastLeaveTime).values(id, currentTime).execute() },
                        { s, clause -> clause.where(s.id.eq(id)).set(s.lastJoinTime, currentTime).execute() },
                        this, plugin
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            val currentPlayTime = Math.round(player.getStatistic(Statistic.PLAY_ONE_TICK) * 0.052).toInt()

            try {
                databaseManager.connection.use {
                    QPlayTime.playTime.runQuery(
                        { p, clause -> clause.columns(p.id, p.amount).values(id, currentPlayTime).execute() },
                        { p, clause -> clause.where(p.id.eq(id)).set(p.amount, currentPlayTime).execute() },
                        this, plugin
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            if (player.isSleeping) {
                try {
                    databaseManager.connection.use {
                        QSleep.sleep.runQuery(
                            { s, query ->
                                val map = HashMap<String, Int>()

                                var enterBed = query.from(s).where(s.id.eq(id)).uniqueResult(s.enterBed)
                                enterBed = if (enterBed == null) 0 else enterBed
                                if (enterBed !== 0) {
                                    map["timeSlept"] = currentTime - enterBed
                                } else {
                                    map["timeSlept"] = 0
                                }

                                map
                            }, { s, clause, map ->
                                if (map["timeSlept"] == 0) {
                                    clause.columns(s.id, s.leaveBed).values(id, currentTime).execute()
                                } else {
                                    clause.columns(s.id, s.leaveBed, s.timeSlept).values(id, currentTime, map["timeSlept"]).execute()
                                }
                            }, { s, clause, map ->
                                if (map["timeSlept"] == 0) {
                                    clause.where(s.id.eq(id)).set(s.leaveBed, currentTime).execute()
                                } else {
                                    clause.where(s.id.eq(id)).set(s.leaveBed, currentPlayTime).set(s.timeSlept, map["timeSlept"]).execute()
                                }
                            },
                            this, plugin
                        )
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setupPlayer(player: OfflinePlayer, connection: Connection): Int? {
        val array = player.uniqueId.toByte()
        val name = player.name
        // Check player / id listing
        val p = QPlayers.players
        var query = databaseManager.getNewQuery(connection) ?: return null

        val result = query.from(p).where(p.uuid.eq(array)).uniqueResult(p)

        if (result == null) {
            val update = databaseManager.getUpdateClause(connection, p) ?: return null

            // Blank out any conflicting names
            update.where(p.name.eq(name)).set(p.name, "").execute()
            val insert = databaseManager.getInsertClause(connection, p) ?: return null

            // Insert new player listing
            insert.columns(p.uuid, p.name).values(array, name).execute()

            checkBlanks()
        } else if (result.name != name) {
            val update = databaseManager.getUpdateClause(connection, p) ?: return null

            // Blank out any conflicting names
            update.where(p.name.eq(name)).set(p.name, "").execute()
            // Change name of UUID player
            update.where(p.uuid.eq(array)).set(p.name, name).execute()

            checkBlanks()
        }

        val id = databaseManager.getPlayerId(player.uniqueId)

        if (config.stats.firstJoinTime) {
            query = databaseManager.getNewQuery(connection) ?: return null

            val s = QSeen.seen
            var time = query.from(s).where(s.id.eq(id)).uniqueResult(s.firstJoinTime)
            time = if (time == null) 0 else time
            if (time !== (player.firstPlayed / 1000L).toInt()) {
                try {
                    val clause = databaseManager.getInsertClause(connection, s) ?: return null
                    clause.columns(s.id, s.firstJoinTime).values(id, (player.firstPlayed / 1000L).toInt()).execute()
                } catch (e: QueryException) {
                    val clause = databaseManager.getUpdateClause(connection, s) ?: return null
                    clause.where(s.id.eq(id)).set(s.firstJoinTime, (player.firstPlayed / 1000L).toInt()).execute()
                }

            }
        }

        if (player.isOnline) {
            val currentPlayTime = Math.round((player as Player).getStatistic(Statistic.PLAY_ONE_TICK) * 0.052).toInt()

            QPlayTime.playTime.runQuery(
                { pl, clause -> clause.columns(pl.id, pl.amount).values(id, currentPlayTime).execute() },
                { pl, clause -> clause.where(pl.id.eq(id)).set(pl.amount, currentPlayTime).execute() },
                connection, this
            )
        }

        return id
    }

    private fun getCurrentName(uuid: UUID): String {
        // Get JSON from Mojang API
        val url = "https://api.mojang.com/user/profiles/${uuid.toString().replace("-".toRegex(), "")}/names"

        var s: String? = null
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.inputStream.reader().buffered().use {
                s = readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Parse the JSON, checking for errors as we go
        if (s.isNullOrBlank()) {
            throw IllegalStateException("No response from Mojang API.")
        }
        val list = JSONValue.parse(s)
        if (list !is JSONArray)
            throw IllegalStateException("Mojang API returned incorrect JSON: " + s)

        val last = list[list.size - 1]
        if (last !is JSONObject)
            throw IllegalStateException("Mojang API returned incorrect JSON: " + s)

        if (last.isEmpty()) {
            throw IllegalStateException("Mojang API returned bad name object: " + s)
        }

        val fin = last["name"]
        if (fin !is String) {
            throw IllegalStateException("Mojang API returned bad name object: " + s)
        }

        if (fin.isEmpty()) {
            throw IllegalStateException("Mojang API returned bad name: " + s)
        }

        return fin
    }

    private fun checkBlanks() {
        threadManager.scheduleRaw(QPlayers::class.java, { connection ->
            val p = QPlayers.players
            val query = databaseManager.getNewQuery(connection) ?: return@scheduleRaw

            val result = query.from(p).where(p.name.eq("")).list(p)
            for (players in result) {
                val uuid = players.uuid.toUUID()
                try {
                    val name = getCurrentName(uuid)

                    val update = databaseManager.getUpdateClause(connection, p) ?: return@scheduleRaw
                    update.where(p.uuid.eq(players.uuid)).set(p.name, name).execute()
                } catch (e: Exception) {
                    server.logger.warning("Was unable to set new name for " + uuid.toString())
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * Returns the last time a player was on fire

     * @param uuid The uuid of the player that was on fire
     * *
     * @return The time that the player was last on fire
     */
    fun getLastFireTime(uuid: UUID): Int {
        val time = lastFireTime[uuid]
        return time ?: 0
    }

    /**
     * Returns the last time a player was drowning

     * @param uuid The uuid of the player that was drowning
     * *
     * @return The time that the player was last drowning
     */
    fun getLastDrownTime(uuid: UUID): Int {
        val time = lastDrownTime[uuid]
        return time ?: 0
    }

    /**
     * Returns the last time a player was poisoned

     * @param uuid The uuid of the player that was poisoned
     * *
     * @return The time that the player was last poisoned
     */
    fun getLastPoisonTime(uuid: UUID): Int {
        val time = lastPoisonTime[uuid]
        return time ?: 0
    }

    /**
     * Returns the last time a player was withering away

     * @param uuid The uuid of the player that was withering away
     * *
     * @return The time that the player was last withering away
     */
    fun getLastWitherTime(uuid: UUID): Int {
        val time = lastWitherTime[uuid]
        return time ?: 0
    }

    /**
     * Sets the last time a player is on fire

     * @param uuid The uuid of the player that is on fire
     * *
     * @param time The time the player was on fire
     */
    fun setLastFireTime(uuid: UUID, time: Int) {
        lastFireTime[uuid] = time
    }

    /**
     * Sets the last time a player is drowning

     * @param uuid The uuid of the player that is drowning
     * *
     * @param time The time the player was drowning
     */
    fun setLastDrowningTime(uuid: UUID, time: Int) {
        lastDrownTime[uuid] = time
    }

    /**
     * Sets the last time a player is poisoned

     * @param uuid The uuid of the player that is poisoned
     * *
     * @param time The time the player was poisoned
     */
    fun setLastPoisonTime(uuid: UUID, time: Int) {
        lastPoisonTime[uuid] = time
    }

    /**
     * Sets the last time a player is withering away

     * @param uuid The uuid of the player that is withering awawy
     * *
     * @param time The time the player is withering away
     */
    fun setLastWitherTime(uuid: UUID, time: Int) {
        lastWitherTime[uuid] = time
    }
}
