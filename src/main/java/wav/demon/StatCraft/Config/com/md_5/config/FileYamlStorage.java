package wav.demon.StatCraft.Config.com.md_5.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class FileYamlStorage<T extends AnnotatedConfig> {

    private File file;
    private Class<T> clazz;
    private T config;
    private Plugin plugin;

    public FileYamlStorage(File file, Class<T> clazz, Plugin plugin) {
        this.file = file;
        this.clazz = clazz;
        this.plugin = plugin;
    }

    public T load() {
        try {
            config = new YamlStorageReader<T>(new FileReader(file), plugin).load(clazz);
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
