/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2016 Kyle Wood (DemonWav)
 * https://www.demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config.com.md_5.config;

import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.Reader;

public class YamlStorageReader<T> {

    private Reader reader;

    public YamlStorageReader(Reader reader) {
        this.reader = reader;
    }

    public T load(Class<T> clazz) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(clazz.getClassLoader()));
        T object = yaml.loadAs(reader, clazz);
        if (object == null) {
            try {
                object = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return object;
    }
}
