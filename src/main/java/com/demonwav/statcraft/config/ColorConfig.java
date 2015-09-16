/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config;

import com.demonwav.statcraft.config.com.md_5.config.AnnotatedConfig;
import com.demonwav.statcraft.config.com.md_5.config.ConfigComment;
import com.demonwav.statcraft.config.com.md_5.config.NewLine;

public class ColorConfig extends AnnotatedConfig {

    @ConfigComment({"The color of the @<playername> text that proceeds a public command response, which denotes",
                    "who ran the command."})
    public String public_identifier = "GRAY";

    @NewLine
    @ConfigComment("Color of a player's name")
    public String player_name = "RED";

    @NewLine
    @ConfigComment({"The color of the stat title, which looks like:",
                    "- <stat title> -"})
    public String stat_title = "WHITE";

    @NewLine
    @ConfigComment("The label for the more specific stat, or just \"Total\"")
    public String stat_label = "AQUA";

    @NewLine
    @ConfigComment("The value of the individual statistic")
    public String stat_value = "DARK_AQUA";

    @NewLine
    @ConfigComment("The vertical line that separates individual stats in some commands.")
    public String stat_separator = "BLUE";

    @NewLine
    @ConfigComment("The number that proceeds the listing for -top# commands.")
    public String list_number = "WHITE";
}
