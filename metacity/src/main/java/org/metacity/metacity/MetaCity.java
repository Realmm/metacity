package org.metacity.metacity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.metacity.commands.Command;
import org.metacity.core.CorePlugin;
import org.metacity.metacity.player.PlayerManager;

import java.util.logging.Level;
import java.util.stream.Stream;

public class MetaCity extends CorePlugin {

    private static MetaCity plugin;
    private PlayerManager playerManager;
    private SpigotBootstrap bootstrap;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        plugin = this;

        bootstrap = new SpigotBootstrap(this);
        EnjinCraft.register(bootstrap);
        bootstrap.setUp();

        playerManager = new PlayerManager(bootstrap);

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        bootstrap.tearDown();
        EnjinCraft.unregister();
    }

    private void registerCommands() {

    }

    private void registerListeners() {
        Stream.of(
                playerManager
        ).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
    }

    public static MetaCity getInstance() {
        return plugin;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SpigotBootstrap bootstrap() {
        return bootstrap;
    }

    public void log(Throwable throwable) {
        getLogger().log(Level.WARNING, "Exception Caught", throwable);
    }

}
