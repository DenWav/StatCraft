/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config

import com.md_5.config.ConfigComment
import com.md_5.config.NewLine

data class ColorConfig(
    @ConfigComment("The color of the @<playername> text that proceeds a public command response, which denotes",
        "who ran the command.")
    var publicIdentifier: String = "GRAY",

    @NewLine
    @ConfigComment("Color of a player's name")
    var playerName: String = "RED",

    @NewLine
    @ConfigComment("The color of the stat title, which looks like:",
        "- <stat title> -")
    var statTitle: String = "WHITE",

    @NewLine
    @ConfigComment("The label for the more specific stat, or just \"Total\"")
    var statLabel: String = "AQUA",

    @NewLine
    @ConfigComment("The value of the individual statistic")
    var statValue: String = "DARK_AQUA",

    @NewLine
    @ConfigComment("The vertical line that separates individual stats in some commands.")
    var statSeparator: String = "BLUE",

    @NewLine
    @ConfigComment("The number that proceeds the listing for -top# commands.")
    var listNumber: String = "WHITE"
)
