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

public class Config extends AnnotatedConfig {

    @ConfigComment("MySQL Settings")
    public SqlConfig mysql = new SqlConfig();

    public StatsConfig stats = new StatsConfig();

    @NewLine
    @ConfigComment({"You can specify a server timezone for the last seen command, or just leave it as \"auto\".",
                    "If you specify a timezone, it must be a proper three letter abbreviation."})
    public String timezone = "auto";

    @NewLine
    @ConfigComment({"Change the look of the plugin's command responses by modifying these values.",
                    "For any of the color fields, this is the list of responses you can use:",
                    "",
                    " - BLACK",
                    " - DARK_BLUE",
                    " - DARK_GREEN",
                    " - DARK_AQUA",
                    " - DARK_RED",
                    " - DARK_PURPLE",
                    " - GOLD",
                    " - GRAY",
                    " - DARK_GRAY",
                    " - BLUE",
                    " - GREEN",
                    " - AQUA",
                    " - RED",
                    " - LIGHT_PURPLE",
                    " - YELLOW",
                    " - WHITE"})
    public ColorConfig colors = new ColorConfig();
}
