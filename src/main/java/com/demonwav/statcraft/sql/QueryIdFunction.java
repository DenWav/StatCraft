/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.sql;

import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;

public interface QueryIdFunction<T extends RelationalPath<?>, R> {

    R run(T t, SQLQuery query, int id, int worldId);
}
