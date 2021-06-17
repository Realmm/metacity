package org.metacity.metacity.listeners;

import com.enjin.sdk.models.notification.EventType;
import com.enjin.sdk.models.notification.NotificationEvent;
import com.enjin.sdk.models.request.TransactionType;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;
import org.metacity.util.Logger;

import java.util.Optional;

public class ChainListener implements com.enjin.sdk.services.notification.NotificationListener {

    private final MetaCity plugin;

    public ChainListener() {
        this.plugin = MetaCity.getInstance();
    }

    @Override
    public void notificationReceived(NotificationEvent event) {
        try {
            Logger.debug(String.format("Received event: %s", event));

            EventType eventType = event.getType();
            if (eventType == null)
                return;

            switch (eventType) {
                case TRADE_COMPLETED:
                case TRADE_CREATED:
                    onTradeExecuted(event);
                    break;
                case TRANSACTION_CANCELED:
                    onTxCancelled(event);
                    break;
                case IDENTITY_LINKED:
                    onIdentityLinked(event);
                    break;
                case IDENTITY_DELETED:
                case IDENTITY_UNLINKED:
                    onIdentityUnlinked(event);
                    break;
                case TOKEN_MELTED:
                case TOKEN_MINTED:
                case TOKEN_TRANSFERRED:
                    onTokenUpdated(event);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onTradeExecuted(NotificationEvent event) {
        if (!event.getChannel().contains("app"))
            return;

        TransactionType type = getTransactionType(event);
        if (type == null)
            return;

        int transactionId;
        String tradeId;

        try {
            JsonObject transaction = event.getEventData()
                    .get("transaction")
                    .getAsJsonObject();
            JsonObject trade = event.getEventData()
                    .get("trade")
                    .getAsJsonObject();
            transactionId = transaction.get("id").getAsInt();
            tradeId = trade.get("id").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        switch (type) {
            case CREATE_TRADE:
                plugin.getTradeManager().sendCompleteRequest(transactionId, tradeId);
                break;
            case SEND:
                plugin.getTradeManager().completeTrade(transactionId);
                break;
            default:
                break;
        }
    }

    private void onTxCancelled(NotificationEvent event) {
        if (!event.getChannel().contains("app"))
            return;

        TransactionType type = getTransactionType(event);
        if (type == null)
            return;

        int transactionId;
        try {
            JsonObject transaction = event.getEventData()
                    .get("transaction")
                    .getAsJsonObject();
            transactionId = transaction.get("id").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        switch (type) {
            case CREATE_TRADE:
            case COMPLETE_TRADE:
                plugin.getTradeManager().cancelTrade(transactionId);
                break;
            default:
                break;
        }
    }

    private void onIdentityLinked(NotificationEvent event) {
        if (!event.getChannel().contains("identity"))
            return;

        JsonObject identity = event.getEventData().getAsJsonObject("identity");
        if (identity == null || identity.get("id") == null)
            return;

        int id = identity.get("id").getAsInt();

        MetaPlayer metaPlayer = plugin.getPlayerManager()
                .getPlayer(id)
                .orElse(null);
        if (metaPlayer == null)
            return;

        JsonObject wallet = event.getEventData().getAsJsonObject("wallet");

        metaPlayer.player().ifPresent(p -> {
            Translation.COMMAND_LINK_SUCCESS.send(p, wallet.get("ethAddress").getAsString());
            Bukkit.getScheduler().runTaskAsynchronously(plugin, metaPlayer::reloadIdentity);
        });
    }

    private void onIdentityUnlinked(NotificationEvent event) {
        if (!event.getChannel().contains("identity"))
            return;

        JsonObject identity = event.getEventData().getAsJsonObject("identity");
        if (identity == null || identity.get("id") == null)
            return;

        int id = identity.get("id").getAsInt();

        Optional<MetaPlayer> playerOptional = plugin.getPlayerManager().getPlayer(id);
        playerOptional.ifPresent(player -> {
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    player::unlinked);
        });
    }

    private void onTokenUpdated(NotificationEvent event) {
        if (!event.getChannel().contains("token"))
            return;

        TransactionType type = getTransactionType(event);
        if (type == null)
            return;

        switch (type) {
            case MELT:
            case MINT:
            case SEND:
                updateBalance(type, event.getEventData());
                break;
            default:
                break;
        }
    }

    private void updateBalance(TransactionType type, JsonObject data) {
        String  tokenId;
        String  index;
        boolean nonfungible;
        int     amount;

        try {
            tokenId = data.get("token")
                    .getAsJsonObject()
                    .get("id")
                    .getAsString();
            nonfungible = data.get("token")
                    .getAsJsonObject()
                    .get("nonFungible")
                    .getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (type == TransactionType.SEND) {
            String from;
            String to;

            try {
                JsonObject transfer = data.get("transfer").getAsJsonObject();
                from = transfer.get("from").getAsString();
                to = transfer.get("to").getAsString();
                amount = nonfungible
                        ? 1
                        : transfer.get("value").getAsInt();
                index = nonfungible
                        ? transfer.get("index").getAsString()
                        : null;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            updateBalance(from, tokenId, index, -amount);
            updateBalance(to, tokenId, index, amount);
        } else if (type == TransactionType.MELT) {
            String ethAddr;

            try {
                JsonObject melt = data.get("melt").getAsJsonObject();
                ethAddr = melt.get("owner").getAsString();
                amount = nonfungible
                        ? 1
                        : melt.get("value").getAsInt();
                index = nonfungible
                        ? melt.get("index").getAsString()
                        : null;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            updateBalance(ethAddr, tokenId, index, -amount);
        } else if (type == TransactionType.MINT) {
            String ethAddr;

            try {
                JsonObject mint = data.get("mint").getAsJsonObject();
                JsonObject transaction = data.get("transaction").getAsJsonObject();
                ethAddr = mint.get("to").getAsString();
                amount = nonfungible
                        ? 1
                        : transaction.get("value").getAsInt();

                /* Parses the first parameter which is the only value
                 * with the token's index as a part of it when minting.
                 *
                 * The first parameter is formatted as:
                 *     "Index <token_index> of Non-Fungible <token_name>"
                 */
                if (nonfungible) {
                    String   param = data.get("param1").getAsString();
                    String[] split = param.split(" ");
                    index = split[1];
                } else {
                    index = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            updateBalance(ethAddr, tokenId, index, amount);
        }
    }

    private void updateBalance(String ethAddr, String tokenId, String tokenIndex, int balanceDelta) {
        MetaPlayer metaPlayer = plugin.getPlayerManager()
                .getPlayer(ethAddr)
                .orElse(null);
        if (metaPlayer == null || metaPlayer.getTokenWallet() == null)
            return;

        String         fullId     = TokenUtils.createFullId(tokenId, tokenIndex);
        TokenModel tokenModel = plugin.getTokenManager().getToken(fullId);
        MutableBalance mBalance   = metaPlayer.getTokenWallet().getBalance(fullId);

        if ((mBalance == null || mBalance.balance() == 0) && balanceDelta > 0) {
            mBalance = new MutableBalance(tokenId, tokenIndex, balanceDelta);
            metaPlayer.getTokenWallet().setBalance(mBalance);
            metaPlayer.addTokenPermissions(tokenModel);
        } else if (mBalance != null) {
            int balance = mBalance.add(balanceDelta);
            if (balance == 0) {
                metaPlayer.getTokenWallet().removeBalance(fullId);
                metaPlayer.removeTokenPermissions(tokenModel);
            }
        }

        metaPlayer.validateInventory();
    }

    private TransactionType getTransactionType(NotificationEvent event) {
        String typeString;
        try {
            JsonObject transaction = event.getEventData()
                    .get("transaction")
                    .getAsJsonObject();
            typeString = transaction.get("type").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        for (TransactionType transactionType : TransactionType.values()) {
            if (typeString.equalsIgnoreCase(transactionType.name()))
                return transactionType;
        }

        Logger.debug(String.format("No such transaction type: %s", typeString));

        return null;
    }

}
