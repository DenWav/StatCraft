/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.demonwav.statcraft.StatCraft;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class WorkerInstance implements Runnable {

    final private ConcurrentLinkedQueue<Consumer<Connection>> work;
    final private StatCraft plugin;

    public WorkerInstance(final ConcurrentLinkedQueue<Consumer<Connection>> work, final StatCraft plugin) {
        this.work = work;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try (final Connection connection = plugin.getDatabaseManager().getConnection()) {
            Consumer<Connection> consumer = work.poll();
            while (consumer != null) {
                try {
                    consumer.accept(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                consumer = work.poll();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
