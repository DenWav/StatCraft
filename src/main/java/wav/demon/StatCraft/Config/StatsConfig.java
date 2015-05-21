package wav.demon.StatCraft.Config;

import wav.demon.StatCraft.Config.com.md_5.config.AnnotatedConfig;
import wav.demon.StatCraft.Config.com.md_5.config.ConfigComment;
import wav.demon.StatCraft.Config.com.md_5.config.NewLine;

public class StatsConfig extends AnnotatedConfig {

    @ConfigComment("Record deaths")
    public boolean deaths = true;

    @NewLine
    @ConfigComment("Record when a player breaks/places a block.")
    public boolean blocks = true;
    @ConfigComment("Allow players to search specific blocks broken on players (/mined command).")
    public boolean specific_blocks = true;

    @NewLine
    @ConfigComment({"Keep track of how long players are online.",
                    "last_seen and joins require that this must be true."})
    public boolean play_time = true;
    @ConfigComment({"Allow players to check when a player was last online.",
                    "play_time must be true to enable this."})
    public boolean last_seen = true;
    @ConfigComment({"Keep track of the number of times a player joins the server.",
                    "play_time must be true to enable this."})
    public boolean joins = true;
    @ConfigComment("Record the date when a player first joined joined the server.")
    public boolean first_join_time = true;

    @NewLine
    @ConfigComment("Record number of items a player has crafted.")
    public boolean items_crafted = true;

    @NewLine
    @ConfigComment("Record number of items a player has brewed.")
    public boolean items_brewed = true;

    @NewLine
    @ConfigComment("Record number of items a player has cooked.")
    public boolean items_cooked = true;

    @NewLine
    @ConfigComment("Record how long a player has been on fire.")
    public boolean on_fire = true;
    @ConfigComment("Announce to the server whenever a player is on fire.")
    public boolean on_fire_announce = true;
    @ConfigComment({"Message to show to the players when an on_fire announcement is made.",
                    "~ is the player's name."})
    public String on_fire_announce_message = "~  is on fire! Oh no!";

    @NewLine
    @ConfigComment("Record number of times a player changes worlds (such as nether portals).")
    public boolean world_changes = true;

    @NewLine
    @ConfigComment("Record when a player breaks a tool.")
    public boolean tools_broken = true;

    @NewLine
    @ConfigComment("Record number of arrows shot.")
    public boolean arrows_shot = true;

    @NewLine
    @ConfigComment("Record when a player fills a bucket.")
    public boolean buckets_filled = true;
    @ConfigComment("Record whe na player empties a bucket.")
    public boolean buckets_emptied = true;

    @NewLine
    @ConfigComment("Record when a player drops an item.")
    public boolean item_drops = true;
    @ConfigComment("Record when a player picks up an item.")
    public boolean item_pickups = true;

    @NewLine
    @ConfigComment({"Record the number of times a player enters and leaves a bed.",
                    "This includes time slept."})
    public boolean bed = true;

    @NewLine
    @ConfigComment("Record the number of messages a player has spoken.")
    public boolean messages_spoken = true;
    @ConfigComment({"Record the total number of words a player has spoken.",
                    "messages_spoken must be true to enable this."})
    public boolean words_spoken = true;
    @ConfigComment({"Record specific words spoken by players.",
                    "words_spoken must be true to enable this."})
    public boolean specific_words_spoken = true;

    @NewLine
    @ConfigComment("Record the number of attempted tab-completes a player does.")
    public boolean tab_completes = true;

    @NewLine
    @ConfigComment("Record the total damage a player has received.")
    public boolean damage_taken = true;
    @ConfigComment("Announce to the server whenever a player is drowning.")
    public boolean drowning_announce = true;
    @ConfigComment("Announce to the server whenever a player is poisoned.")
    public boolean poison_announce = true;
    @ConfigComment("Announce to the server whenever a player is withering away.")
    public boolean wither_announce = true;
    @ConfigComment({"Message to show to the players when a drowning announcement is made.",
                    "~ is the player's name."})
    public String drown_announce_message = "~ is drowning! Oh no!";
    @ConfigComment({"Message to show to the players when a poison announcement is made.",
                    "~ is the player's name."})
    public String poison_announce_message = "~ is poisoned! Oh no!";
    @ConfigComment({"Message to show to the players when a withering away announcement is made.",
                    "~ is the player's name."})
    public String wither_announce_message = "~ is withering away! Oh no!";

    @NewLine
    @ConfigComment("Record when a player catches a fish.")
    public boolean fish_caught = true;

    @NewLine
    @ConfigComment("Record the total xp gained by a player.")
    public boolean xp_gained = true;
    @ConfigComment("Record the total xp spent by a player (enchanting, repairing).")
    public boolean xp_spent = true;
    @ConfigComment("Record the highest leve a player has obtained.")
    public boolean highest_level = true;

    @NewLine
    @ConfigComment("Keep track of how far a player has moved (uses server stats).")
    public boolean move = true;

    @NewLine
    @ConfigComment("Record when a player kills a mob or another player.")
    public boolean kills = true;

    @NewLine
    @ConfigComment("Record how many times a player has jumped.")
    public boolean jumps = true;

    @NewLine
    @ConfigComment("Track the number of eggs thrown.")
    public boolean eggs_thrown = true;

    @NewLine
    @ConfigComment("Record stats on ender pearls.")
    public boolean ender_pearls = true;
    @ConfigComment("Record stats on snowballs.")
    public boolean snow_balls = true;

    @NewLine
    @ConfigComment("Record how many animals a player has killed.")
    public boolean animals_bred = true;

    @NewLine
    @ConfigComment("Record how many blocks of TNT a player has set off.")
    public boolean tnt_detonated = true;

    @NewLine
    @ConfigComment("Record how many times a player has enchanted an item.")
    public boolean enchants_done = true;

    @NewLine
    @ConfigComment("Record how many times a player has repaired an item.")
    public boolean repairs_done = true;

    @NewLine
    @ConfigComment("Track the amount of damage a player has dealt to mobs and other players.")
    public boolean damage_dealt = true;

    @NewLine
    @ConfigComment("Track how many times a player has started a fire.")
    public boolean fires_started = true;
}
