package org.metacity.metacity;

import org.bukkit.Bukkit;
import org.metacity.core.CorePlugin;
import org.metacity.metacity.cmd.enj.MetaCmd;
import org.metacity.metacity.listeners.QrItemListener;
import org.metacity.metacity.listeners.TokenItemListener;
import org.metacity.metacity.player.PlayerManager;
import org.metacity.metacity.storage.ChainDatabase;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.trade.TradeManager;
import org.metacity.metacity.trade.TradeUpdateTask;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.metacity.world.Generator;

import java.util.stream.Stream;

public class MetaCity extends CorePlugin {

    private static MetaCity plugin;
    private PlayerManager playerManager;
    private Generator generator;

    private TokenManager tokenManager;
    private ChainDatabase database;
    private Chain chain;


    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        plugin = this;

        setUp();

        playerManager = new PlayerManager();
        generator = new Generator("metacity");

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tearDown();
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

    public Chain chain() {
        return chain;
    }

    public Generator generator() {
        return generator;
    }

    private void setUp() {
        try {
            plugin.saveDefaultConfig();

            database = new ChainDatabase();

            // Create the trusted platform client
            chain = new Chain();

            // Init Managers
            playerManager = new PlayerManager();
            tokenManager = new TokenManager();
            tradeManager = new TradeManager();
            tokenManager.loadTokens();
            tokenManager.loadLocalTokens();


            // Register Listeners
            Bukkit.getPluginManager().registerEvents(playerManager, plugin);
            Bukkit.getPluginManager().registerEvents(tradeManager, plugin);
            Bukkit.getPluginManager().registerEvents(new TokenItemListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new QrItemListener(), plugin);

            // Register Commands
//            PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand("meta"),
//                    "Missing \"meta\" command definition in plugin.yml");
//            CmdMeta cmdMeta = new CmdMeta();
//            pluginCommand.setExecutor(cmdMeta);
            new MetaCmd().register();

//            Command.builder(Player.class, "meta")
//                    .addSubCommand(b -> b.build())
//                    .build().register();

            new TradeUpdateTask().runTaskTimerAsynchronously(plugin, 20, 20);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void tearDown() {
        chain.stop();
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    private boolean validateConfig() {
        boolean validAppId = MetaConfig.getAppId() >= 0;
        boolean validSecret = !MetaConfig.getAppSecret().isEmpty();
        boolean validDevAddress = MetaConfig.WALLET_ADDRESS != null && !MetaConfig.WALLET_ADDRESS.isEmpty();

        if (!validAppId)
            plugin.getLogger().warning("Invalid app id specified in config.");
        if (!validSecret)
            plugin.getLogger().warning("Invalid app secret specified in config.");
        if (!validDevAddress)
            plugin.getLogger().warning("Invalid dev address specified in config.");

        return validAppId && validSecret && validDevAddress;
    }

    public ChainDatabase db() {
        return database;
    }
}
