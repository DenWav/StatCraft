/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.demonwav.statcraft.StatCraft;
import com.mysema.query.QueryException;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  This class ensures thread safety among tables, it will process each individual table on it's own thread,
 *  but it will ensure that all work on all threads of this plugin does work on each table synchronously.
 *  This will prevent multiple events firing for multiple players and multiple locations from causing thread
 *  errors on frequently accessed tables.
 */
public class ThreadManager implements Runnable {

    final private StatCraft plugin;

    final private ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<Runnable>> map = new ConcurrentHashMap<>();
    final private ConcurrentHashMap<Class<?>, Integer> work = new ConcurrentHashMap<>();

    public ThreadManager(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Remove any work that has finished
        work.entrySet().removeIf(e -> !Bukkit.getScheduler().isCurrentlyRunning(e.getValue()));

        // Start work that is waiting to be started
        for (Iterator<Map.Entry<Class<?>, ConcurrentLinkedQueue<Runnable>>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class<?>, ConcurrentLinkedQueue<Runnable>> entry = it.next();

            if (!work.containsKey(entry.getKey())) {
                work.put(
                    entry.getKey(),
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                        new WorkerInstance(entry.getValue())
                    ).getTaskId()
                );
                it.remove();
            }
        }
    }

    public <T extends RelationalPath<?>> void schedule(final Class<T> clazz,
                                                       final UUID uuid,
                                                       final QueryIdRunner<T, SQLInsertClause> insertRunner,
                                                       final QueryIdRunner<T, SQLUpdateClause> updateRunner) {
        final QueryRunnable<T> queryRunnable = new QueryRunnable<>(clazz, uuid, insertRunner, updateRunner, plugin);
        scheduleRaw(clazz, queryRunnable);
    }

    public <T extends RelationalPath<?>, K, V> void schedule(final Class<T> clazz,
                                                             final UUID uuid,
                                                             final QueryIdFunction<T, K, V> workBefore,
                                                             final QueryIdRunnerMap<T, SQLInsertClause, K, V> insertRunner,
                                                             final QueryIdRunnerMap<T, SQLUpdateClause, K, V> updateRunner) {
        final QueryRunnableMap<T, K, V> queryRunnableMap = new QueryRunnableMap<>(clazz, uuid, workBefore, insertRunner, updateRunner, plugin);
        scheduleRaw(clazz, queryRunnableMap);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void scheduleRaw(final Class<?> clazz, final Runnable runnable) {
        ConcurrentLinkedQueue<Runnable> queue;
        synchronized (clazz) {
            queue = map.get(clazz);
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                map.put(clazz, queue);
            }
        }
        queue.offer(runnable);
    }

    public void stop() {
        // We need to get all the work finished as quickly as possible
        work.clear();
        map.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .forEach(e -> {
                ConcurrentLinkedQueue<Runnable> queue = e.getValue();
                Runnable runnable = queue.poll();
                while (runnable != null) {
                    runnable.run();
                    runnable = queue.poll();
                }
            });
        map.clear();
    }

    private class WorkerInstance implements Runnable {

        final private ConcurrentLinkedQueue<Runnable> work;

        public WorkerInstance(final ConcurrentLinkedQueue<Runnable> work) {
            this.work = work;
        }

        @Override
        public void run() {
            Runnable runnable = work.poll();
            while (runnable != null) {
                try {
                    runnable.run();
                } catch (QueryException e) {
                    // This is more than likely cased by a connection exception
                    plugin.incrementError();
                    e.printStackTrace();
                } catch (Exception e) {
                    // This is an issue unrelated to the database
                    e.printStackTrace();
                }
                runnable = work.poll();
            }
        }
    }
}

