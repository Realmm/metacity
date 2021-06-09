package org.metacity.metacity;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.TrustedPlatformClientBuilder;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.AccessToken;
import com.enjin.sdk.models.balance.Balance;
import com.enjin.sdk.models.balance.GetBalances;
import com.enjin.sdk.models.identity.CreateIdentity;
import com.enjin.sdk.models.identity.GetIdentities;
import com.enjin.sdk.models.identity.Identity;
import com.enjin.sdk.models.identity.UnlinkIdentity;
import com.enjin.sdk.models.platform.GetPlatform;
import com.enjin.sdk.models.platform.PlatformDetails;
import com.enjin.sdk.models.request.CreateRequest;
import com.enjin.sdk.models.request.GetRequests;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.data.AdvancedSendTokenData;
import com.enjin.sdk.models.request.data.CompleteTradeData;
import com.enjin.sdk.models.request.data.CreateTradeData;
import com.enjin.sdk.models.request.data.SendTokenData;
import com.enjin.sdk.models.request.data.TokenValueData;
import com.enjin.sdk.models.request.data.TransferData;
import com.enjin.sdk.models.token.GetToken;
import com.enjin.sdk.models.token.Token;
import com.enjin.sdk.models.user.CreateUser;
import com.enjin.sdk.models.user.GetUsers;
import com.enjin.sdk.models.user.User;
import com.enjin.sdk.models.wallet.Wallet;
import com.enjin.sdk.services.notification.NotificationsService;
import com.enjin.sdk.services.notification.PusherNotificationService;
import com.enjin.sdk.utils.LoggerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.exceptions.AuthenticationException;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.exceptions.NotificationServiceException;
import org.metacity.metacity.listeners.ChainListener;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.trade.TradeSession;
import org.metacity.metacity.util.StringUtils;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Chain {

    private final TrustedPlatformClient client;
    private PlatformDetails platformDetails;
    private NotificationsService notificationsService;

    public Chain() {
        client = new TrustedPlatformClientBuilder()
                .baseUrl(MetaConfig.DEV_MODE ? TrustedPlatformClientBuilder.KOVAN : TrustedPlatformClientBuilder.MAIN_NET)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        authClient();
        long interval = TimeUnit.HOURS.toMillis(6) / 50;
        Bukkit.getScheduler().runTaskTimerAsynchronously(MetaCity.getInstance(), this::authClient, interval, interval);
        fetchPlatformDetails();
        startNotificationService();
    }

    public void updateWallet(MetaPlayer p, Consumer<Wallet> consumer) {
        if (!p.wallet().isPresent()) throw new IllegalStateException("No wallet present, must load identity first");
        p.wallet().ifPresent(w -> {
            if (StringUtils.isEmpty(w.getEthAddress()))
                throw new IllegalStateException("Wallet address is empty, unable to update wallet");
        });
        Wallet w = p.wallet().get();

        try {
            client
                    .getBalanceService()
                    .getBalancesAsync(new GetBalances()
                            .valGt(0)
                            .ethAddress(w.getEthAddress()), r -> {
                        if (!r.isSuccess())
                            throw new NetworkException(r.code());

                        GraphQLResponse<List<Balance>> graphQLResponse = r.body();
                        if (!graphQLResponse.isSuccess())
                            throw new GraphQLException(graphQLResponse.getErrors());

                        p.getTokenWallet().addBalances(graphQLResponse.getData());
                        consumer.accept(w);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlink(MetaPlayer p, Consumer<Identity> consumer) {
        if (!p.isLinked() || p.getIdentityId() == null) return;
        client
                .getIdentityService()
                .unlinkIdentityAsync(new UnlinkIdentity().id(p.getIdentityId()), r -> {
                    if (!r.isSuccess())
                        throw new NetworkException(r.code());

                    GraphQLResponse<Identity> graphQLResponse = r.body();
                    if (!graphQLResponse.isSuccess())
                        throw new GraphQLException(graphQLResponse.getErrors());

                    consumer.accept(graphQLResponse.getData());
                });
    }

    public void updateIdentity(MetaPlayer p, Consumer<Identity> consumer) {
        if (p.getIdentityId() == null) {
            // Create the Identity for the App ID and Player in question
            client.getIdentityService()
                    .createIdentityAsync(new CreateIdentity()
                            .appId(client.getAppId())
                            .userId(p.getUserId())
                            .withLinkingCode()
                            .withLinkingCodeQr(), r -> {
                        if (!r.isSuccess())
                            throw new NetworkException(r.code());

                        GraphQLResponse<Identity> graphQLResponse = r.body();
                        if (!graphQLResponse.isSuccess())
                            throw new GraphQLException(graphQLResponse.getErrors());

                        consumer.accept(graphQLResponse.getData());
                    });
        } else {
            client.getIdentityService()
                    .getIdentitiesAsync(new GetIdentities()
                            .identityId(p.getIdentityId())
                            .withLinkingCode()
                            .withLinkingCodeQr()
                            .withWallet(), r -> {
                        if (!r.isSuccess())
                            throw new NetworkException(r.code());

                        GraphQLResponse<List<Identity>> graphQLResponse = r.body();
                        if (!graphQLResponse.isSuccess())
                            throw new GraphQLException(graphQLResponse.getErrors());

                        if (!graphQLResponse.getData().isEmpty())
                            consumer.accept(graphQLResponse.getData().get(0));
                    });
        }
    }

    public void updateUser(MetaPlayer p, Consumer<User> consumer) {
        // Fetch the User for the Player in question
        client
                .getUserService()
                .getUsersAsync(new GetUsers()
                        .name(p.uuid().toString())
                        .withUserIdentities()
                        .withLinkingCode()
                        .withLinkingCodeQr()
                        .withWallet(), r -> {
                    if (!r.isSuccess())
                        throw new NetworkException(r.code());

                    GraphQLResponse<List<User>> graphQLResponse = r.body();
                    if (!graphQLResponse.isSuccess())
                        throw new GraphQLException(graphQLResponse.getErrors());

                    User user = null;
                    if (!graphQLResponse.getData().isEmpty()) {
                        user = graphQLResponse.getData().get(0);
                        consumer.accept(user);
                    }

                    if (user == null) {
                        client
                                .getUserService().createUserAsync(new CreateUser()
                                .name(p.uuid().toString())
                                .withUserIdentities()
                                .withLinkingCode()
                                .withLinkingCodeQr(), re -> {
                            if (!re.isSuccess())
                                throw new NetworkException(re.code());

                            GraphQLResponse<User> response = re.body();
                            if (!response.isSuccess())
                                throw new GraphQLException(response.getErrors());

                            consumer.accept(response.getData());
                        });
                    }
                });
    }

    public void listenToIdentity(int id) {
        boolean listening = notificationsService.isSubscribedToIdentity(id);
        if (!listening) {
            try {
                notificationsService.subscribeToIdentity(id);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCompleteRequest(TradeSession session, String tradeId) {
        if (session == null || StringUtils.isEmpty(tradeId))
            return;

        Optional<Player> inviter = Optional.ofNullable(Bukkit.getPlayer(session.getInviterUuid()));
        Optional<Player> invitee = Optional.ofNullable(Bukkit.getPlayer(session.getInvitedUuid()));

        client.getRequestService().createRequestAsync(new CreateRequest()
                        .appId(client.getAppId())
                        .identityId(session.getInvitedIdentityId())
                        .completeTrade(CompleteTradeData.builder()
                                .tradeId(tradeId)
                                .build()),
                networkResponse -> {
                    if (!networkResponse.isSuccess())
                        throw new NetworkException(networkResponse.code());

                    GraphQLResponse<Transaction> graphQLResponse = networkResponse.body();
                    if (!graphQLResponse.isSuccess())
                        throw new GraphQLException(graphQLResponse.getErrors());

                    Transaction dataIn = graphQLResponse.getData();
                    inviter.ifPresent(Translation.COMMAND_TRADE_CONFIRM_WAIT::send);
                    invitee.ifPresent(Translation.COMMAND_TRADE_CONFIRM_ACTION::send);

                    try {
                        MetaCity.getInstance().db().completeTrade(session.getCreateRequestId(), dataIn.getId(), tradeId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void send(MetaPlayer inviter, MetaPlayer invitee, List<ItemStack> tokens) {
        CreateRequest input = new CreateRequest()
                .appId(client.getAppId())
                .identityId(inviter.getIdentityId());

        if (tokens.size() == 1) {
            ItemStack is = tokens.get(0);
            SendTokenData.SendTokenDataBuilder builder = SendTokenData.builder();
            builder.recipientIdentityId(invitee.getIdentityId())
                    .tokenId(TokenUtils.getTokenID(is))
                    .value(is.getAmount());

            if (TokenUtils.isNonFungible(is))
                builder.tokenIndex(TokenUtils.getTokenIndex(is));

            input.sendToken(builder.build());
        } else {
            List<TransferData> transfers = new ArrayList<>();

            for (ItemStack is : tokens) {
                TransferData.TransferDataBuilder builder = TransferData.builder()
                        .fromId(inviter.getIdentityId())
                        .toId(invitee.getIdentityId())
                        .tokenId(TokenUtils.getTokenID(is))
                        .value(String.valueOf(is.getAmount()));

                if (TokenUtils.isNonFungible(is))
                    builder.tokenIndex(TokenUtils.getTokenIndex(is));

                transfers.add(builder.build());
            }

            input.advancedSendToken(AdvancedSendTokenData.builder()
                    .transfers(transfers)
                    .build());
        }

        client.getRequestService().createRequestAsync(input, r -> {
            if (!r.isSuccess())
                throw new NetworkException(r.code());

            GraphQLResponse<Transaction> graphQLResponse = r.body();
            if (!graphQLResponse.isSuccess())
                throw new GraphQLException(graphQLResponse.getErrors());

            invitee.player().ifPresent(p -> {
                Translation.COMMAND_TRADE_CONFIRM_WAIT.send(p);
                Translation.COMMAND_TRADE_CONFIRM_ACTION.send(p);
            });
        });
    }

    public void createTradeRequest(MetaPlayer inviter, MetaPlayer invitee, List<TokenValueData> playerOneTokens, List<TokenValueData> playerTwoTokens) {
        client.getRequestService().createRequestAsync(new CreateRequest()
                        .appId(client.getAppId())
                        .identityId(inviter.getIdentityId())
                        .createTrade(CreateTradeData.builder()
                                .offeringTokens(playerOneTokens)
                                .askingTokens(playerTwoTokens)
                                .secondPartyIdentityId(invitee.getIdentityId())
                                .build()),
                r -> {
                    try {
                        if (!r.isSuccess())
                            throw new NetworkException(r.code());

                        GraphQLResponse<Transaction> graphQLResponse = r.body();
                        if (!graphQLResponse.isSuccess())
                            throw new GraphQLException(graphQLResponse.getErrors());

                        Transaction dataIn = graphQLResponse.getData();
                        invitee.player().ifPresent(p -> {
                            Translation.COMMAND_TRADE_CONFIRM_WAIT.send(p);
                            Translation.COMMAND_TRADE_CONFIRM_ACTION.send(p);
                        });

                        MetaCity.getInstance().db().createTrade(inviter.uuid(),
                                inviter.getIdentityId(),
                                inviter.getEthereumAddress(),
                                invitee.uuid(),
                                invitee.getIdentityId(),
                                invitee.getEthereumAddress(),
                                dataIn.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void subscribeToToken(TokenModel tokenModel) {
        if (notificationsService != null && !notificationsService.isSubscribedToToken(tokenModel.getId()))
            notificationsService.subscribeToToken(tokenModel.getId());
    }

    public void unsubscribeToToken(TokenModel tokenModel) {
        if (notificationsService != null && notificationsService.isSubscribedToToken(tokenModel.getId()))
            notificationsService.unsubscribeToToken(tokenModel.getId());
    }

    public void getMostRecentTransaction(TradeSession session, Consumer<List<Transaction>> consumer) {
        client
                .getRequestService().getRequestsAsync(new GetRequests()
                .requestId(session.getMostRecentRequestId())
                .withEvents()
                .withState(), r -> {
            if (!r.isSuccess())
                throw new NetworkException(r.code());

            GraphQLResponse<List<Transaction>> graphQLResponse = r.body();
            if (!graphQLResponse.isSuccess())
                throw new GraphQLException(graphQLResponse.getErrors());

            consumer.accept(graphQLResponse.getData());
        });
    }

    public void updateMetadataURI(String id) throws GraphQLException, NetworkException {
        TokenManager manager = MetaCity.getInstance().getTokenManager();
        TokenModel tokenModel = manager.getToken(id);
        if (tokenModel == null)
            return;

        client.getTokenService().getTokenAsync(new GetToken()
                .tokenId(tokenModel.getId())
                .withItemUri(), r -> {
            if (!r.isSuccess())
                throw new NetworkException(r.code());

            GraphQLResponse<Token> graphQLResponse = r.body();
            if (!graphQLResponse.isSuccess())
                throw new GraphQLException(graphQLResponse.getErrors());

            String metadataURI = graphQLResponse.getData().getItemURI();
            manager.updateMetadataURI(tokenModel.getId(), metadataURI);
        });
    }

    private void authClient() {
        try {
            // Attempt to authenticate the client using an app secret
            client.authAppAsync(MetaConfig.getAppId(), MetaConfig.getAppSecret(), r -> {
                if (!r.isSuccess()) {
                    // Could not authenticate the client
                    throw new AuthenticationException(r.code());
                } else if (r.body().isSuccess()) {
                    Logger.info("[CONNECTED] Ethereum Blockchain Authenticated");
                }
            });
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    protected void stop() {
        try {
            if (client != null)
                client.close();
            if (notificationsService != null)
                notificationsService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchPlatformDetails() {
        try {
            // Fetch the platform details
            client.getPlatformService()
                    .getPlatformAsync(new GetPlatform().withNotificationDrivers(), r -> {
                        if (!r.isSuccess())
                            throw new NetworkException(r.code());

                        GraphQLResponse<PlatformDetails> graphQLResponse = r.body();
                        if (!graphQLResponse.isSuccess())
                            throw new GraphQLException(graphQLResponse.getErrors());

                        platformDetails = graphQLResponse.getData();
                    });
        } catch (Exception ex) {
            throw new NetworkException(ex);
        }
    }

    private void startNotificationService() {
        try {
            // Start the notification service and register a listener
            notificationsService = new PusherNotificationService(new LoggerProvider(Bukkit.getLogger(),
                    false,
                    Level.INFO), platformDetails);
            notificationsService.start();
            notificationsService.registerListener(new ChainListener());
            notificationsService.subscribeToApp(MetaConfig.getAppId());
        } catch (Exception ex) {
            throw new NotificationServiceException(ex);
        }
    }

}
