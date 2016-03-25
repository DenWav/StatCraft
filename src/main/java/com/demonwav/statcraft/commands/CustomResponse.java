/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands;

import org.bukkit.command.CommandSender;

public interface CustomResponse {

    void respondToCommand(final CommandSender sender,  String[] args);
}
