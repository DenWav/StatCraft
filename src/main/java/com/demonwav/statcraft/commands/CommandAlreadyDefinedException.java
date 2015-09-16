/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands;

public class CommandAlreadyDefinedException extends RuntimeException {
    public CommandAlreadyDefinedException(String s) {
        super(s + " has already been defined.");
    }
}
