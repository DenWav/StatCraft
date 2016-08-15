/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config

import com.demonwav.statcraft.config.com.md_5.config.ConfigComment
import com.demonwav.statcraft.config.com.md_5.config.NewLine

data class StatsConfig(
    @ConfigComment("Record deaths")
    var deaths: Boolean = true,

    @NewLine
    @ConfigComment("Record when a player breaks/places a block.")
    var blocks: Boolean = true,
    @ConfigComment("Allow players to search specific blocks broken on players (/mined command).")
    var specificBlocks: Boolean = true,

    @NewLine
    @ConfigComment("Keep track of how long players are online.", "last_seen and joins require that this must be true.")
    var playTime: Boolean = true,
    @ConfigComment("Allow players to check when a player was last online.", "play_time must be true to enable this.")
    var lastSeen: Boolean = true,
    @ConfigComment("Keep track of the number of times a player joins the server.", "play_time must be true to enable this.")
    var joins: Boolean = true,
    @ConfigComment("Record the date when a player first joined joined the server.")
    var firstJoinTime: Boolean = true,

    @NewLine
    @ConfigComment("Record number of items a player has crafted.")
    var itemsCrafted: Boolean = true,

    @NewLine
    @ConfigComment("Record number of items a player has brewed.")
    var itemsBrewed: Boolean = true,

    @NewLine
    @ConfigComment("Record number of items a player has cooked.")
    var itemsCooked: Boolean = true,

    @NewLine
    @ConfigComment("Record how long a player has been on fire.")
    var onFire: Boolean = true,
    @ConfigComment("Announce to the server whenever a player is on fire.")
    var onFireAnnounce: Boolean = true,
    @ConfigComment("Message to show to the players when an on_fire announcement is made.", "~ is the player's name.")
    var onFireAnnounceMessage: String = "~ is on fire! Oh no!",

    @NewLine
    @ConfigComment("Record number of times a player changes worlds (such as nether portals).")
    var worldChanges: Boolean = true,

    @NewLine
    @ConfigComment("Record when a player breaks a tool.")
    var toolsBroken: Boolean = true,

    @NewLine
    @ConfigComment("Record number of arrows shot.")
    var arrowsShot: Boolean = true,

    @NewLine
    @ConfigComment("Record when a player fills a bucket.")
    var bucketsFilled: Boolean = true,
    @ConfigComment("Record whe na player empties a bucket.")
    var bucketsEmptied: Boolean = true,

    @NewLine
    @ConfigComment("Record when a player drops an item.")
    var itemDrops: Boolean = true,
    @ConfigComment("Record when a player picks up an item.")
    var itemPickUps: Boolean = true,

    @NewLine
    @ConfigComment("Record the number of times a player enters and leaves a bed.", "This includes time slept.")
    var bed: Boolean = true,

    @NewLine
    @ConfigComment("Record the number of messages a player has spoken.")
    var messagesSpoken: Boolean = true,
    @ConfigComment("Record the total number of words a player has spoken.", "messages_spoken must be true to enable this.")
    var wordsSpoken: Boolean = true,
    @ConfigComment("Record specific words spoken by players.", "words_spoken must be true to enable this.")
    var specificWordsSpoken: Boolean = true,

    @NewLine
    @ConfigComment("Record the number of attempted tab-completes a player does.")
    var tabCompletes: Boolean = true,

    @NewLine
    @ConfigComment("Record the total damage a player has received.")
    var damageTaken: Boolean = true,
    @ConfigComment("Announce to the server whenever a player is drowning.")
    var drowningAnnounce: Boolean = true,
    @ConfigComment("Announce to the server whenever a player is poisoned.")
    var poisonAnnounce: Boolean = true,
    @ConfigComment("Announce to the server whenever a player is withering away.")
    var witherAnnounce: Boolean = true,
    @ConfigComment("Message to show to the players when a drowning announcement is made.", "~ is the player's name.")
    var drownAnnounceMessage: String = "~ is drowning! Oh no!",
    @ConfigComment("Message to show to the players when a poison announcement is made.", "~ is the player's name.")
    var poisonAnnounceMessage: String = "~ is poisoned! Oh no!",
    @ConfigComment("Message to show to the players when a withering away announcement is made.", "~ is the player's name.")
    var witherAnnounceMessage: String = "~ is withering away! Oh no!",

    @NewLine
    @ConfigComment("Record when a player catches a fish.")
    var fishCaught: Boolean = true,

    @NewLine
    @ConfigComment("Record the total xp gained by a player.")
    var xpGained: Boolean = true,
    @ConfigComment("Record the total xp spent by a player (enchanting, repairing).")
    var xpSpent: Boolean = true,
    @ConfigComment("Record the highest leve a player has obtained.")
    var highestLevel: Boolean = true,

    @NewLine
    @ConfigComment("Keep track of how far a player has moved (uses server stats).")
    var move: Boolean = true,

    @NewLine
    @ConfigComment("Record when a player kills a mob or another player.")
    var kills: Boolean = true,

    @NewLine
    @ConfigComment("Record how many times a player has jumped.")
    var jumps: Boolean = true,

    @NewLine
    @ConfigComment("Track the number of eggs thrown.")
    var eggsThrown: Boolean = true,

    @NewLine
    @ConfigComment("Record stats on ender pearls.")
    var enderPearls: Boolean = true,
    @ConfigComment("Record stats on snowballs.")
    var snowBalls: Boolean = true,

    @NewLine
    @ConfigComment("Record how many animals a player has killed.")
    var animalsBred: Boolean = true,

    @NewLine
    @ConfigComment("Record how many blocks of TNT a player has set off.")
    var tntDetonated: Boolean = true,

    @NewLine
    @ConfigComment("Record how many times a player has enchanted an item.")
    var enchantsDone: Boolean = true,

    @NewLine
    @ConfigComment("Record how many times a player has repaired an item.")
    var repairsDone: Boolean = true,

    @NewLine
    @ConfigComment("Track the amount of damage a player has dealt to mobs and other players.")
    var damageDealt: Boolean = true,

    @NewLine
    @ConfigComment("Track how many times a player has started a fire.")
    var firesStarted: Boolean = true,

    @NewLine
    @ConfigComment("Track how many times a player has been kicked and the reasons")
    var kicks: Boolean = true
)
