/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config.com.md_5.config;

public class StandaloneComment {

    public String[] value;

    public StandaloneComment(String... lines) {
        value = lines;
    }

    public StandaloneComment(String line) {
        value = new String[] {line};
    }
}
