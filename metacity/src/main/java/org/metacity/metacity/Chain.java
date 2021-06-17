package org.metacity.metacity;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.TrustedPlatformClientBuilder;
import com.enjin.sdk.graphql.GraphQLResponse;
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
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.metacity.metacity.conversations.Conversations;
import org.metacity.metacity.conversations.prompts.TokenIdPrompt;
import org.metacity.metacity.conversations.prompts.TokenIndexPrompt;
import org.metacity.metacity.conversations.prompts.TokenNicknamePrompt;
import org.metacity.metacity.conversations.prompts.TokenTypePrompt;
import org.metacity.metacity.exceptions.AuthenticationException;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.exceptions.NotificationServiceException;
import org.metacity.metacity.listeners.ChainListener;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.trade.TradeSession;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        authClient(() -> fetchPlatformDetails(this::startNotificationService));
        long interval = TimeUnit.HOURS.toMillis(6) / 50;
        Bukkit.getScheduler().runTaskTimerAsynchronously(MetaCity.getInstance(), () -> authClient(null), interval, interval);
    }

    public void sendToken(@Nullable MetaPlayer sender, String targetAddress, String tokenId, @Nullable String tokenIndex, int amount) {
        MetaCity.getInstance().getPlayerManager().getPlayer(targetAddress).ifPresent(target ->
                sendToken(sender, target, tokenId, tokenIndex, amount));
    }

    public void sendToken(@Nullable MetaPlayer sender, MetaPlayer target, String tokenId, @Nullable String tokenIndex, int amount) {
        if (sender != null && !sender.identity().isPresent())
            throw new IllegalStateException("No wallet address loaded for sender");
        if (!target.identity().isPresent()) throw new IllegalStateException("No wallet address loaded for target");
        TokenModel token = MetaCity.getInstance().getTokenManager().getToken(tokenId);
        if (token == null) throw new IllegalStateException("No token matching the provided id");
        if (token.isNonfungible() && amount > 1)
            throw new IllegalStateException("Unable to send more than 1 of a non-fungible instance");

        SendTokenData data = tokenIndex == null
                ? SendTokenData.builder()
                .recipientIdentityId(target.getIdentityId())
                .tokenId(tokenId)
                .value(amount)
                .build()
                : SendTokenData.builder()
                .recipientIdentityId(target.getIdentityId())
                .tokenId(tokenId)
                .tokenIndex(tokenIndex)
                .value(amount)
                .build();

        String walletAddress = sender == null ?
                MetaConfig.WALLET_ADDRESS :
                sender.identity().get().getWallet().getEthAddress();

        CommandSender s = sender == null ? Bukkit.getConsoleSender() : sender.player().orElseThrow(() ->
                new IllegalStateException("No sender for sending token"));

        client.getRequestService().createRequestAsync(new CreateRequest()
                        .appId(client.getAppId())
                        .ethAddr(walletAddress)
                        .sendToken(data),
                r -> {
                    if (!r.isSuccess()) {
                        NetworkException exception = new NetworkException(r.code());
                        Translation.ERRORS_EXCEPTION.send(s, exception.getMessage());
                        throw exception;
                    }

                    GraphQLResponse<Transaction> graphQLResponse = r.body();
                    if (!graphQLResponse.isSuccess()) {
                        GraphQLException exception = new GraphQLException(graphQLResponse.getErrors());
                        Translation.ERRORS_EXCEPTION.send(s, exception.getMessage());
                        throw exception;
                    }

                    Translation.COMMAND_SEND_SUBMITTED.send(s);
                });
    }

    public void startConversation(MetaPlayer player, ItemStack item, String tokenId) {
        player.player().ifPresent(sender -> {
            TokenManager tm = MetaCity.getInstance().getTokenManager();
            TokenModel model = tm.getToken(tokenId);

            if (model == null) {
                client.getTokenService().getTokenAsync(new GetToken().tokenId(tokenId),
                        r -> {
                            if (r.isEmpty()) return;

                            GraphQLResponse<Token> resGql = r.body();
                            if (!resGql.isSuccess()) return;

                            Token token = resGql.getData();
                            Bukkit.getScheduler().runTask(MetaCity.getInstance(), () ->
                                    startConversation(sender, item, tokenId, token.getNonFungible(), false));
                        });
            } else {
                startConversation(sender, item, model.getId(), model.isNonfungible(), true);
            }
        });
    }

    private void startConversation(Conversable sender, ItemStack ref, String id, boolean nft, boolean baseExists) {
        // Setup Conversation
        Conversations conversations = new Conversations(MetaCity.getInstance(), nft, baseExists);
        Conversation conversation = conversations.startTokenCreationConversation(sender);
        conversation.addConversationAbandonedListener(this::executeAbandonedListener);
        conversation.getContext().setSessionData("sender", sender);
        conversation.getContext().setSessionData("nbt-item", NBTItem.convertItemtoNBT(ref));
        conversation.getContext().setSessionData(TokenTypePrompt.KEY, nft);
        conversation.getContext().setSessionData(TokenIdPrompt.KEY, id);
        conversation.begin();
    }

    private void executeAbandonedListener(ConversationAbandonedEvent event) {
        // Check if the conversation completed gracefully.
        if (!event.gracefulExit()) return;

        // Load managers and data store
        Map<Object, Object> data = event.getContext().getAllSessionData();
        TokenManager tokenManager = MetaCity.getInstance().getTokenManager();
        // Load data from conversation context
        Player sender = (Player) data.get("sender");
        boolean nft = (boolean) data.get(TokenTypePrompt.KEY);
        String id = (String) data.get(TokenIdPrompt.KEY);
        BigInteger index = (BigInteger) data.getOrDefault(TokenIndexPrompt.KEY, BigInteger.ZERO);
        // Convert index from decimal to hexadecimal representation
        String indexHex = index == null ? null : TokenUtils.bigIntToIndex(index);

        // Check whether the token can be created if another already exists.
        // This will only ever pass if the token is an nft, the index is non-zero
        // and doesn't exist in the database.
        if (tokenManager.hasToken(id)) {
            TokenModel base = tokenManager.getToken(id);

            if (base.isNonfungible() && !nft) {
                Translation.COMMAND_TOKEN_ISFUNGIBLE.send(sender);
                return;
            } else if (!base.isNonfungible()) {
                Translation.COMMAND_TOKEN_CREATE_DUPLICATE.send(sender);
                return;
            } else if (tokenManager.hasToken(TokenUtils.createFullId(id, indexHex))) {
                Translation.COMMAND_TOKEN_CREATENFT_DUPLICATE.send(sender);
                return;
            }
        } else if (nft && !index.equals(BigInteger.ZERO)) {
            Translation.COMMAND_TOKEN_CREATENFT_MISSINGBASE.send(sender);
            return;
        }

        // Start token model creation process
        NBTContainer nbt = (NBTContainer) data.get("nbt-item");
        TokenModel.TokenModelBuilder modelBuilder = TokenModel.builder()
                .id(id)
                .nonfungible(nft)
                .nbt(nbt.toString());

        // Add index if creating an nft
        if (nft) {
            modelBuilder.index(indexHex);
        }

        // Validate and add nickname if present
        if (data.containsKey(TokenNicknamePrompt.KEY)) {
            String nickname = (String) data.get(TokenNicknamePrompt.KEY);

            if (!TokenManager.isValidAlternateId(nickname)) {
                Translation.COMMAND_TOKEN_NICKNAME_INVALID.send(sender);
                return;
            }

            modelBuilder.alternateId(nickname);
        }

        // Create token model and save to database
        TokenModel model = modelBuilder.build();
        int result = tokenManager.saveToken(model);

        // Inform sender of result or log to console if unknown
        switch (result) {
            case TokenManager.TOKEN_CREATE_SUCCESS:
                Translation.COMMAND_TOKEN_CREATE_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_FAILED:
                Translation.COMMAND_TOKEN_CREATE_FAILED.send(sender);
                break;
            case TokenManager.TOKEN_ALREADYEXISTS:
                Translation translation = nft
                        ? Translation.COMMAND_TOKEN_CREATENFT_DUPLICATE
                        : Translation.COMMAND_TOKEN_CREATE_DUPLICATE;
                translation.send(sender);
                break;
            case TokenManager.TOKEN_INVALIDDATA:
                Translation.COMMAND_TOKEN_INVALIDDATA.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_FAILEDNFTBASE:
                Translation.COMMAND_TOKEN_CREATENFT_BASEFAILED.send(sender);
                break;
            case TokenManager.TOKEN_DUPLICATENICKNAME:
                Translation.COMMAND_TOKEN_NICKNAME_DUPLICATE.send(sender);
                break;
            case TokenManager.TOKEN_INVALIDNICKNAME:
                Translation.COMMAND_TOKEN_NICKNAME_INVALID.send(sender);
                break;
            default:
                Logger.debug(String.format("Unhandled result when creating token (status: %d)", result));
                break;
        }
    }

    public void getURI(CommandSender sender, String tokenId) {
        client.getTokenService().getTokenAsync(new GetToken()
                        .tokenId(tokenId)
                        .withItemUri(),
                r -> {
                    if (!r.isSuccess()) {
                        NetworkException exception = new NetworkException(r.code());
                        Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                        throw exception;
                    }

                    GraphQLResponse<Token> graphQLResponse = r.body();
                    if (!graphQLResponse.isSuccess()) {
                        GraphQLException exception = new GraphQLException(graphQLResponse.getErrors());
                        Translation.ERRORS_EXCEPTION.send(sender, exception.toString());
                        throw exception;
                    }

                    String metadataURI = graphQLResponse.getData().getItemURI();
                    if (metadataURI.isEmpty()) {
                        Translation.COMMAND_TOKEN_GETURI_EMPTY_1.send(sender);
                        Translation.COMMAND_TOKEN_GETURI_EMPTY_2.send(sender);
                        return;
                    }

                    int result = MetaCity.getInstance().getTokenManager().updateMetadataURI(tokenId, metadataURI);
                    switch (result) {
                        case TokenManager.TOKEN_NOSUCHTOKEN:
                            Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(sender);
                            break;
                        case TokenManager.TOKEN_UPDATE_SUCCESS:
                            Translation.COMMAND_TOKEN_GETURI_SUCCESS.send(sender);
                            break;
                        case TokenManager.TOKEN_ISNOTBASE:
                            Translation.COMMAND_TOKEN_ISNONFUNGIBLEINSTANCE.send(sender);
                            break;
                        case TokenManager.TOKEN_UPDATE_FAILED:
                            Translation.COMMAND_TOKEN_GETURI_FAILED.send(sender);
                            break;
                        default:
                            Logger.debug(String.format("Unhandled result when getting the URI (status: %d)", result));
                            break;
                    }
                });
    }

    /**
     * Update the players wallet cache
     * This will only take effect if the players wallet has been loaded by loading the players identity
     * and the player has already linked their wallet
     * @param p The player to update
     * @param consumer The consumer to accept the wallet if found
     */
    public void updateWallet(MetaPlayer p, Consumer<Wallet> consumer) {
        if (!p.wallet().isPresent() || p.wallet().get().getEthAddress().isEmpty()) return;

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
        if (session == null || tradeId.isEmpty())
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

    public void sendTrade(MetaPlayer inviter, MetaPlayer invitee, List<ItemStack> tokens) {
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

    private void authClient(@Nullable Runnable onAuth) {
        try {
            // Attempt to authenticate the client using an app secret
            client.authAppAsync(MetaConfig.getAppId(), MetaConfig.getAppSecret(), r -> {
                if (!r.isSuccess()) {
                    // Could not authenticate the client
                    throw new AuthenticationException(r.code());
                } else if (r.body().isSuccess()) {
                    Logger.info("[CONNECTED] Ethereum Blockchain Authenticated");
                    if (onAuth != null) onAuth.run();
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

    private void fetchPlatformDetails(@Nullable Runnable onComplete) {
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
                        if (onComplete != null) onComplete.run();
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

    public void stopNotificationService(MetaPlayer p) {
        p.identity().ifPresent(i -> {
            boolean listening = notificationsService.isSubscribedToIdentity(i.getId());
            if (listening) notificationsService.unsubscribeToIdentity(i.getId());
        });
    }

}
