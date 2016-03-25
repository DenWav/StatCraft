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

public interface QueryRunnerMap<T extends RelationalPath<?>, S, R> {
    void run(T t, S s, R r);
}
