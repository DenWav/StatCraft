/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.commands

import org.bukkit.command.CommandSender

interface CustomResponse {

    fun respondToCommand(sender: CommandSender, args: Array<String>)
}
