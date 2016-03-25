/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config;

import com.demonwav.statcraft.config.com.md_5.config.AnnotatedConfig;
import com.demonwav.statcraft.config.com.md_5.config.ConfigComment;
import com.demonwav.statcraft.config.com.md_5.config.NewLine;
import lombok.Data;

@Data
public class StatsConfig extends AnnotatedConfig {

    @ConfigComment("Record deaths")
    private boolean deaths = true;

    @NewLine
    @ConfigComment("Record when a player breaks/places a block.")
    private boolean blocks = true;
    @ConfigComment("Allow players to search specific blocks broken on players (/mined command).")
    private boolean specificBlocks = true;

    @NewLine
    @ConfigComment({"Keep track of how long players are online.",
                    "last_seen and joins require that this must be true."})
    private boolean playTime = true;
    @ConfigComment({"Allow players to check when a player was last online.",
                    "play_time must be true to enable this."})
    private boolean lastSeen = true;
    @ConfigComment({"Keep track of the number of times a player joins the server.",
                    "play_time must be true to enable this."})
    private boolean joins = true;
    @ConfigComment("Record the date when a player first joined joined the server.")
    private boolean firstJoinTime = true;

    @NewLine
    @ConfigComment("Record number of items a player has crafted.")
    private boolean itemsCrafted = true;

    @NewLine
    @ConfigComment("Record number of items a player has brewed.")
    private boolean itemsBrewed = true;

    @NewLine
    @ConfigComment("Record number of items a player has cooked.")
    private boolean itemsCooked = true;

    @NewLine
    @ConfigComment("Record how long a player has been on fire.")
    private boolean onFire = true;
    @ConfigComment("Announce to the server whenever a player is on fire.")
    private boolean onFireAnnounce = true;
    @ConfigComment({"Message to show to the players when an on_fire announcement is made.",
                    "~ is the player's name."})
    private String onFireAnnounceMessage = "~ is on fire! Oh no!";

    @NewLine
    @ConfigComment("Record number of times a player changes worlds (such as nether portals).")
    private boolean worldChanges = true;

    @NewLine
    @ConfigComment("Record when a player breaks a tool.")
    private boolean toolsBroken = true;

    @NewLine
    @ConfigComment("Record number of arrows shot.")
    private boolean arrowsShot = true;

    @NewLine
    @ConfigComment("Record when a player fills a bucket.")
    private boolean bucketsFilled = true;
    @ConfigComment("Record whe na player empties a bucket.")
    private boolean bucketsEmptied = true;

    @NewLine
    @ConfigComment("Record when a player drops an item.")
    private boolean itemDrops = true;
    @ConfigComment("Record when a player picks up an item.")
    private boolean itemPickUps = true;

    @NewLine
    @ConfigComment({"Record the number of times a player enters and leaves a bed.",
                    "This includes time slept."})
    private boolean bed = true;

    @NewLine
    @ConfigComment("Record the number of messages a player has spoken.")
    private boolean messagesSpoken = true;
    @ConfigComment({"Record the total number of words a player has spoken.",
                    "messages_spoken must be true to enable this."})
    private boolean wordsSpoken = true;
    @ConfigComment({"Record specific words spoken by players.",
                    "words_spoken must be true to enable this."})
    private boolean specificWordsSpoken = true;

    @NewLine
    @ConfigComment("Record the number of attempted tab-completes a player does.")
    private boolean tabCompletes = true;

    @NewLine
    @ConfigComment("Record the total damage a player has received.")
    private boolean damageTaken = true;
    @ConfigComment("Announce to the server whenever a player is drowning.")
    private boolean drowningAnnounce = true;
    @ConfigComment("Announce to the server whenever a player is poisoned.")
    private boolean poisonAnnounce = true;
    @ConfigComment("Announce to the server whenever a player is withering away.")
    private boolean witherAnnounce = true;
    @ConfigComment({"Message to show to the players when a drowning announcement is made.",
                    "~ is the player's name."})
    private String drownAnnounceMessage = "~ is drowning! Oh no!";
    @ConfigComment({"Message to show to the players when a poison announcement is made.",
                    "~ is the player's name."})
    private String poisonAnnounceMessage = "~ is poisoned! Oh no!";
    @ConfigComment({"Message to show to the players when a withering away announcement is made.",
                    "~ is the player's name."})
    private String witherAnnounceMessage = "~ is withering away! Oh no!";

    @NewLine
    @ConfigComment("Record when a player catches a fish.")
    private boolean fishCaught = true;

    @NewLine
    @ConfigComment("Record the total xp gained by a player.")
    private boolean xpGained = true;
    @ConfigComment("Record the total xp spent by a player (enchanting, repairing).")
    private boolean xpSpent = true;
    @ConfigComment("Record the highest leve a player has obtained.")
    private boolean highestLevel = true;

    @NewLine
    @ConfigComment("Keep track of how far a player has moved (uses server stats).")
    private boolean move = true;

    @NewLine
    @ConfigComment("Record when a player kills a mob or another player.")
    private boolean kills = true;

    @NewLine
    @ConfigComment("Record how many times a player has jumped.")
    private boolean jumps = true;

    @NewLine
    @ConfigComment("Track the number of eggs thrown.")
    private boolean eggsThrown = true;

    @NewLine
    @ConfigComment("Record stats on ender pearls.")
    private boolean enderPearls = true;
    @ConfigComment("Record stats on snowballs.")
    private boolean snowBalls = true;

    @NewLine
    @ConfigComment("Record how many animals a player has killed.")
    private boolean animalsBred = true;

    @NewLine
    @ConfigComment("Record how many blocks of TNT a player has set off.")
    private boolean tntDetonated = true;

    @NewLine
    @ConfigComment("Record how many times a player has enchanted an item.")
    private boolean enchantsDone = true;

    @NewLine
    @ConfigComment("Record how many times a player has repaired an item.")
    private boolean repairsDone = true;

    @NewLine
    @ConfigComment("Track the amount of damage a player has dealt to mobs and other players.")
    private boolean damageDealt = true;

    @NewLine
    @ConfigComment("Track how many times a player has started a fire.")
    private boolean firesStarted = true;

    @NewLine
    @ConfigComment("Track how many times a player has been kicked and the reasons")
    private boolean kicks = true;
}
