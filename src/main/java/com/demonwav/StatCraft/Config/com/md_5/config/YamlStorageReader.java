/*
 * StatCraft Bukkit Plugin
 *
 * Copyright (c) 2015 Kyle Wood (DemonWav)
 * http://demonwav.com
 *
 * MIT License
 */

package com.demonwav.statcraft.config.com.md_5.config;

import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.Reader;

public class YamlStorageReader<T extends AnnotatedConfig> {

    private Reader reader;

    public YamlStorageReader(Reader reader, Plugin plugin) {
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
