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
public class SqlConfig extends AnnotatedConfig {

    private String hostname = "localhost";
    private String username = "statcraft";
    private String password = "";
    private String database = "statcraft";
    private String port = "3306";

    @NewLine
    @ConfigComment({"StatCraft will attempt to setup the database when it starts, but if there are already conflicting",
                    "tables that are improperly setup, it will only drop those tables if this settings is set to `true`.",
                    "StatCraft will not run unless all of the tables are setup correctly. It is advised to give StatCraft",
                    "its own database to work with, which is the simplest way to prevent conflicts."})
    private boolean forceSetup = false;
}
