package org.metacity.metacity;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.TrustedPlatformClientBuilder;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.AccessToken;
import com.enjin.sdk.models.platform.GetPlatform;
import com.enjin.sdk.models.platform.PlatformDetails;
import com.enjin.sdk.services.notification.NotificationsService;
import com.enjin.sdk.services.notification.PusherNotificationService;
import com.enjin.sdk.utils.LoggerProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.metacity.metacity.cmd.enjin.CmdEnj;
import org.metacity.metacity.exceptions.AuthenticationException;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.exceptions.NotificationServiceException;
import org.metacity.metacity.listeners.EnjEventListener;
import org.metacity.metacity.listeners.QrItemListener;
import org.metacity.metacity.listeners.TokenItemListener;
import org.metacity.metacity.player.PlayerManager;
import org.metacity.metacity.storage.Database;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.trade.TradeManager;
import org.metacity.metacity.trade.TradeUpdateTask;
import org.metacity.metacity.util.StringUtils;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.util.Logger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpigotBootstrap implements Bootstrap, Module {

    public static final long AUTHENTICATION_INTERVAL = TimeUnit.HOURS.toMillis(6) / 50;

    private final MetaCity plugin;
    private TokenManager tokenManager;
    private Database database;

    private TrustedPlatformClient trustedPlatformClient;
    private PlatformDetails platformDetails;
    private NotificationsService notificationsService;
    private PlayerManager playerManager;
    private TradeManager tradeManager;

    public SpigotBootstrap(MetaCity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setUp() {
        try {
            plugin.saveDefaultConfig();

            database = new Database(this);

            // Create the trusted platform client
            trustedPlatformClient = new TrustedPlatformClientBuilder()
                    .baseUrl(MetaConfig.DEV_MODE ? TrustedPlatformClientBuilder.KOVAN : TrustedPlatformClientBuilder.MAIN_NET)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            authenticateTPClient();
            AuthenticationTask authenticationTask = new AuthenticationTask(this);
            authenticationTask.runTaskTimerAsynchronously(plugin, AUTHENTICATION_INTERVAL, AUTHENTICATION_INTERVAL);
            fetchPlatformDetails();
            startNotificationService();

            // Init Managers
            playerManager = new PlayerManager(this);
            tokenManager = new TokenManager(this);
            tradeManager = new TradeManager(this);
            tokenManager.loadTokens();

            // Register Listeners
            Bukkit.getPluginManager().registerEvents(playerManager, plugin);
            Bukkit.getPluginManager().registerEvents(tradeManager, plugin);
            Bukkit.getPluginManager().registerEvents(new TokenItemListener(this), plugin);
            Bukkit.getPluginManager().registerEvents(new QrItemListener(this), plugin);

            // Register Commands
            PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand("enj"),
                    "Missing \"enj\" command definition in plugin.yml");
            CmdEnj cmdEnj = new CmdEnj(this);
            pluginCommand.setExecutor(cmdEnj);

            new TradeUpdateTask(this).runTaskTimerAsynchronously(plugin, 20, 20);
        } catch (Exception ex) {
            log(ex);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    protected void authenticateTPClient() {
        HttpResponse<GraphQLResponse<AccessToken>> networkResponse;

        try {
            // Attempt to authenticate the client using an app secret
            networkResponse = trustedPlatformClient.authAppSync(MetaConfig.getAppId(), MetaConfig.getAppSecret());
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }

        // Could not authenticate the client
        if (!networkResponse.isSuccess()) {
            throw new AuthenticationException(networkResponse.code());
        } else if (networkResponse.body().isSuccess()) {
            getLogger().info("SDK Authenticated!");
        }
    }

    private void fetchPlatformDetails() {
        try {
            // Fetch the platform details
            HttpResponse<GraphQLResponse<PlatformDetails>> networkResponse = trustedPlatformClient.getPlatformService()
                    .getPlatformSync(new GetPlatform().withNotificationDrivers());
            if (!networkResponse.isSuccess())
                throw new NetworkException(networkResponse.code());

            GraphQLResponse<PlatformDetails> graphQLResponse = networkResponse.body();
            if (!graphQLResponse.isSuccess())
                throw new GraphQLException(graphQLResponse.getErrors());

            platformDetails = graphQLResponse.getData();
        } catch (Exception ex) {
            throw new NetworkException(ex);
        }
    }

    private void startNotificationService() {
        try {
            // Start the notification service and register a listener
            notificationsService = new PusherNotificationService(new LoggerProvider(getLogger(),
                    false,
                    Level.INFO), platformDetails);
            notificationsService.start();
            notificationsService.registerListener(new EnjEventListener(this));
            notificationsService.subscribeToApp(MetaConfig.getAppId());
        } catch (Exception ex) {
            throw new NotificationServiceException(ex);
        }
    }

    @Override
    public void tearDown() {
        try {
            if (trustedPlatformClient != null)
                trustedPlatformClient.close();
            if (notificationsService != null)
                notificationsService.shutdown();
        } catch (Exception ex) {
            log(ex);
        }
    }

    @Override
    public TrustedPlatformClient getTrustedPlatformClient() {
        return trustedPlatformClient;
    }

    @Override
    public NotificationsService getNotificationsService() {
        return notificationsService;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    @Override
    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public Plugin plugin() {
        return plugin;
    }

    private boolean validateConfig() {
        boolean validAppId = MetaConfig.getAppId() >= 0;
        boolean validSecret = !StringUtils.isEmpty(MetaConfig.getAppSecret());
        boolean validDevAddress = MetaConfig.WALLET_ADDRESS != null && !MetaConfig.WALLET_ADDRESS.isEmpty();

        if (!validAppId)
            plugin.getLogger().warning("Invalid app id specified in config.");
        if (!validSecret)
            plugin.getLogger().warning("Invalid app secret specified in config.");
        if (!validDevAddress)
            plugin.getLogger().warning("Invalid dev address specified in config.");

        return validAppId && validSecret && validDevAddress;
    }

    public void debug(String log) {
        Logger.debug(log);
    }

    public java.util.logging.Logger getLogger() {
        return plugin.getLogger();
    }

    public void log(Throwable throwable) {
        plugin.log(throwable);
    }

    public Database db() {
        return database;
    }
}
