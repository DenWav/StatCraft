/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.mysema.query.sql.RelationalPath;

import java.util.Map;

public interface QueryRunnerMap<T extends RelationalPath<?>, S, K, V> {
    void run(T t, S s, Map<K, V> map);
}
