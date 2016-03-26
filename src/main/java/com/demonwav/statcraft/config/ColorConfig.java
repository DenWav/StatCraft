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
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ColorConfig extends AnnotatedConfig {

    @ConfigComment({"The color of the @<playername> text that proceeds a public command response, which denotes",
                    "who ran the command."})
    private String publicIdentifier = "GRAY";

    @NewLine
    @ConfigComment("Color of a player's name")
    private String playerName = "RED";

    @NewLine
    @ConfigComment({"The color of the stat title, which looks like:",
                    "- <stat title> -"})
    private String statTitle = "WHITE";

    @NewLine
    @ConfigComment("The label for the more specific stat, or just \"Total\"")
    private String statLabel = "AQUA";

    @NewLine
    @ConfigComment("The value of the individual statistic")
    private String statValue = "DARK_AQUA";

    @NewLine
    @ConfigComment("The vertical line that separates individual stats in some commands.")
    private String statSeparator = "BLUE";

    @NewLine
    @ConfigComment("The number that proceeds the listing for -top# commands.")
    private String listNumber = "WHITE";
}
