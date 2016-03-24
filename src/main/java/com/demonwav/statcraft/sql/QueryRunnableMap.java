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
import com.demonwav.statcraft.Util;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import java.util.UUID;

public class QueryRunnableMap<T extends RelationalPath<?>, K, V> implements Runnable {
    private final Class<T> clazz;
    private final UUID uuid;
    private final QueryIdRunnerMap<T, SQLInsertClause, K, V> insertClause;
    private final QueryIdRunnerMap<T, SQLUpdateClause, K, V> updateClause;
    private final StatCraft plugin;
    private final QueryIdFunction<T, K, V> workBefore;

    public QueryRunnableMap(final Class<T> clazz,
                            final UUID uuid,
                            final QueryIdFunction<T, K, V> workBefore,
                            final QueryIdRunnerMap<T, SQLInsertClause, K, V> insertClause,
                            final QueryIdRunnerMap<T, SQLUpdateClause, K, V> updateClause,
                            final StatCraft plugin) {
        this.clazz = clazz;
        this.uuid = uuid;
        this.workBefore = workBefore;
        this.insertClause = insertClause;
        this.updateClause = updateClause;
        this.plugin = plugin;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Override
    public void run() {
        Util.runQuery(clazz, uuid, workBefore, insertClause, updateClause, plugin);
    }
}
