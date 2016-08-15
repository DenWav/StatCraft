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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class FileYamlStorage<T> {

    private File file;
    private Class<T> clazz;
    private T config;

    public FileYamlStorage(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
    }

    public T load() {
        try {
            config = new YamlStorageReader<T>(new FileReader(file)).load(clazz);
        } catch (Exception ex) {
            try {
                config = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void save() {
        file.getParentFile().mkdirs();
        try {
            new YamlStorageWriter(new PrintWriter(file)).save(config);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
