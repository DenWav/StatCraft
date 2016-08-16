/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql

import com.demonwav.statcraft.StatCraft
import com.demonwav.statcraft.use
import java.sql.Connection
import java.util.concurrent.ConcurrentLinkedQueue

class WorkerInstance(private val work: ConcurrentLinkedQueue<(Connection) -> Unit>, private val plugin: StatCraft) : Runnable {

    override fun run() {
        try {
            plugin.databaseManager.connection.use {
                var consumer = work.poll()
                while (consumer != null) {
                    try {
                        consumer(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    consumer = work.poll()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
