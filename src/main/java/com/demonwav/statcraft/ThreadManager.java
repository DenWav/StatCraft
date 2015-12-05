/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft;

import com.mysema.query.QueryException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  This class ensures thread safety among tables, it will process each individual table on it's own thread,
 *  but it will ensure that all work on all threads of this plugin does work on each table synchronously.
 *  This will prevent multiple events firing for multiple players and multiple locations from causing thread
 *  errors on frequently accessed tables.
 */
public class ThreadManager implements Runnable {

    final private StatCraft plugin;

    final private ConcurrentHashMap<Class<?>, List<Runnable>> map = new ConcurrentHashMap<>();
    final private ConcurrentHashMap<Class<?>, Integer> work = new ConcurrentHashMap<>();

    public ThreadManager(StatCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Remove any work that has finished
        for (Iterator<Map.Entry<Class<?>, Integer>> it = work.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class<?>, Integer> entry = it.next();

            if (!plugin.getServer().getScheduler().isCurrentlyRunning(entry.getValue())) {
                it.remove();
            }
        }

        // Start work that is waiting to be started
        for (Iterator<Map.Entry<Class<?>, List<Runnable>>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class<?>, List<Runnable>> entry = it.next();

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

    public synchronized void schedule(Class<?> clazz, Runnable runnable) {
        List<Runnable> list = map.get(clazz);
        if (list == null) {
            list = new LinkedList<>();
            list.add(runnable);
            map.put(clazz, list);
        } else {
            list.add(runnable);
        }
    }

    public void stop() {
        // We need to get all the work finished as quickly as possible, but we
        // can't use threads here (onDisable() restriction)
        work.clear();
        for (Map.Entry<Class<?>, List<Runnable>> entry : map.entrySet()) {
            if (entry.getValue() != null && entry.getValue().size() != 0) {
                for (Runnable runnable : entry.getValue()) {
                    runnable.run();
                }
            }
        }
        map.clear();
    }

    private class WorkerInstance implements Runnable {

        final private List<Runnable> list;
        final private StatCraft plugin;

        public WorkerInstance(List<Runnable> list, StatCraft plugin) {
            this.list = list;
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Runnable runnable : list) {
                try {
                    runnable.run();
                } catch (QueryException e) {
                    // This is more than likely cause by a connection exception
                    plugin.incrementError();
                    e.printStackTrace();
                }
            }
        }
    }

}
