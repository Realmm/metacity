package org.metacity.metacity;

import org.bukkit.Bukkit;
import org.metacity.commands.CommandNode;
import org.metacity.core.CorePlugin;
import org.metacity.metacity.cmd.chain.LinkCmd;
import org.metacity.metacity.cmd.chain.UnlinkCmd;
import org.metacity.metacity.cmd.chain.wallet.WalletCmd;
import org.metacity.metacity.cmd.chain.wallet.BalanceCmd;
import org.metacity.metacity.cmd.chain.wallet.send.DevSendCmd;
import org.metacity.metacity.cmd.chain.wallet.send.SendCmd;
import org.metacity.metacity.cmd.chain.wallet.trade.TradeCmd;
import org.metacity.metacity.cmd.enj.MetaCmd;
import org.metacity.metacity.listeners.QrItemListener;
import org.metacity.metacity.listeners.TokenItemListener;
import org.metacity.metacity.mmo.MMOManager;
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
    private MMOManager mmoManager;
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
        tradeManager = new TradeManager();
        mmoManager = new MMOManager();
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
        Stream.of(
                new MetaCmd(),
                new UnlinkCmd(),
                new LinkCmd(),
                new WalletCmd(),
                new BalanceCmd(),
                new TradeCmd(),
                new SendCmd(),
                new DevSendCmd()
        ).forEach(c -> c.register());
    }

    private void registerListeners() {
        Stream.of(
                playerManager,
                tradeManager,
                mmoManager,
                new TokenItemListener(),
                new QrItemListener()
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
            tokenManager = new TokenManager();

//            tokenManager.loadTokens(); //Load from database initially
            tokenManager.loadLocalTokens(); //Tries to save implemented tokens in database, updating if necessary
            tokenManager.loadTokens(); //Pull current, potentially updated token data from database

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
