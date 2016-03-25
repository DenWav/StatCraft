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
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 *  This class ensures thread safety among tables, it will process each individual table on it's own thread,
 *  but it will ensure that all work on all threads of this plugin does work on each table synchronously.
 *  This will prevent multiple events firing for multiple players and multiple locations from causing thread
 *  errors on frequently accessed tables.
 */
public class ThreadManager implements Runnable {

    final private StatCraft plugin;

    final private ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Consumer<Connection>>> map = new ConcurrentHashMap<>();
    final private ConcurrentHashMap<Class<?>, Integer> work = new ConcurrentHashMap<>();

    public ThreadManager(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Remove any work that has finished
        work.entrySet().removeIf(e -> !Bukkit.getScheduler().isCurrentlyRunning(e.getValue()));

        // Start work that is waiting to be started
        for (Iterator<Map.Entry<Class<?>, ConcurrentLinkedQueue<Consumer<Connection>>>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class<?>, ConcurrentLinkedQueue<Consumer<Connection>>> entry = it.next();

            if (!work.containsKey(entry.getKey())) {
                work.put(
                    entry.getKey(),
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                        new WorkerInstance(entry.getValue(), plugin)
                    ).getTaskId()
                );
                it.remove();
            }
        }
    }

    public <T extends RelationalPath<?>> void schedule(final Class<T> clazz,
                                                       final UUID playerId,
                                                       final UUID worldId,
                                                       final QueryIdRunner<T, SQLInsertClause> insertRunner,
                                                       final QueryIdRunner<T, SQLUpdateClause> updateRunner) {
        final QueryRunnable<T> queryRunnable = new QueryRunnable<>(clazz, playerId, worldId, insertRunner, updateRunner, plugin);
        scheduleRaw(clazz, queryRunnable);
    }

    public <T extends RelationalPath<?>, R> void schedule(final Class<T> clazz,
                                                             final UUID playerId,
                                                             final UUID worldId,
                                                             final QueryIdFunction<T, R> workBefore,
                                                             final QueryIdRunnerMap<T, SQLInsertClause, R> insertRunner,
                                                             final QueryIdRunnerMap<T, SQLUpdateClause, R> updateRunner) {
        final QueryRunnableMap<T, R> queryRunnableMap = new QueryRunnableMap<>(clazz, playerId, worldId, workBefore, insertRunner, updateRunner, plugin);
        scheduleRaw(clazz, queryRunnableMap);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void scheduleRaw(final Class<?> clazz, final Consumer<Connection> consumer) {
        ConcurrentLinkedQueue<Consumer<Connection>> queue;
        synchronized (clazz) {
            queue = map.get(clazz);
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                map.put(clazz, queue);
            }
        }
        queue.offer(consumer);
    }

    public void stop() {
        // We need to get all the work finished as quickly as possible
        work.clear();
        map.entrySet().parallelStream()
            .filter(e -> e.getValue() != null)
            .forEach(e -> {
                ConcurrentLinkedQueue<Consumer<Connection>> queue = e.getValue();
                try (final Connection connection = plugin.getDatabaseManager().getConnection()) {
                    Consumer<Connection> consumer = queue.poll();
                    while (consumer != null) {
                        consumer.accept(connection);
                        consumer = queue.poll();
                    }
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            });
        map.clear();
    }
}

