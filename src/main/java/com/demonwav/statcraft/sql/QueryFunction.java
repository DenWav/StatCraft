/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;

import java.util.Map;

public interface QueryFunction<T extends RelationalPath<?>, K, V> {

    Map<K, V> run(T t, SQLQuery query);
}
