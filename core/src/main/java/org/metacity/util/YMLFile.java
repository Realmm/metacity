package org.metacity.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

/**
 * Utility class for creating YMLFiles easier
 */
public class YMLFile {

    private FileConfiguration data = null;
    private File file = null;
    private final String name;
    private final Plugin plugin;

    /**
     * Create a YML file
     * @param name The name of the yml file
     */
    public YMLFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        saveDefault();
    }

    /**
     * Reload the YML file
     */
    public void reload() {
        if (file == null) file = new File("plugins/" + plugin.getName(), name + ".yml");
        data = YamlConfiguration.loadConfiguration(file);

        // Look for defaults in the jar
        try {
            Reader ymlStream = new InputStreamReader(plugin.getResource(name + ".yml"), "UTF8");
            YamlConfiguration ymlConfig = YamlConfiguration.loadConfiguration(ymlStream);
            data.setDefaults(ymlConfig);
        } catch (UnsupportedEncodingException e) {
            Logger.error(">> Reload file exception!");
            e.printStackTrace();
        }
    }

    /**
     * Get the files {@link FileConfiguration}
     * @return This files {@link FileConfiguration}
     */
    public FileConfiguration getConfig() {
        if (data == null) reload();
        return data;
    }

    /**
     * Save the YML file
     */
    public void save() {
        try {
            if (file == null || data == null) return;
            data.save(file);
        } catch (IOException e) {
            Logger.error(">> Save file exception!");
            e.printStackTrace();
        }
    }

    /**
     * Remove a certain path from the YML file
     * This updates the YML file automatically after execution
     * @param path The path to remove
     */
    public void remove(String path) {
        setSaveReload(path, null);
    }

    /**
     * Set the object at the path in the YML file, save the file, then reload the file
     * This ensures the YML file in the local directory always matches the YML file in cache
     * @param path The path to set
     * @param obj The object to set at path
     */
    public void setSaveReload(String path, Object obj) {
        getConfig().set(path, obj);
        save();
        reload();
    }

    private void saveDefault() {
        if (file == null) file = new File("plugins/" + plugin.getName(), name + ".yml");
        if (!file.exists()) {
            Logger.info(">> Loading " + name + ".yml!");
            plugin.saveResource(name + ".yml", false);
            Logger.info(">> " + name + ".yml loaded!");
        }
    }

}
