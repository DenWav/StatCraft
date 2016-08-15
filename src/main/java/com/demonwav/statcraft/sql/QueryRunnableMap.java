/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.demonwav.statcraft.StatCraft;
import com.demonwav.statcraft.Util;

import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import java.sql.Connection;
import java.util.UUID;
import java.util.function.Consumer;

public class QueryRunnableMap<T extends RelationalPath<?>, R> implements Consumer<Connection> {
    private final Class<T> clazz;
    private final UUID playerId;
    private final String worldName;
    private final QueryIdRunnerMap<T, SQLInsertClause, R> insertClause;
    private final QueryIdRunnerMap<T, SQLUpdateClause, R> updateClause;
    private final StatCraft plugin;
    private final QueryIdFunction<T, R> workBefore;

    public QueryRunnableMap(final Class<T> clazz,
                            final UUID playerId,
                            final String worldName,
                            final QueryIdFunction<T, R> workBefore,
                            final QueryIdRunnerMap<T, SQLInsertClause, R> insertClause,
                            final QueryIdRunnerMap<T, SQLUpdateClause, R> updateClause,
                            final StatCraft plugin) {
        this.clazz = clazz;
        this.playerId = playerId;
        this.worldName = worldName;
        this.workBefore = workBefore;
        this.insertClause = insertClause;
        this.updateClause = updateClause;
        this.plugin = plugin;
    }

    @Override
    public void accept(final Connection connection) {
        Util.runQuery(clazz, playerId, worldName, workBefore, insertClause, updateClause, connection, plugin);
    }
}
