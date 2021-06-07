package org.metacity.metacity.cmd.enjin.wallet.send;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.models.request.CreateRequest;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.data.SendTokenData;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.cmd.enjin.arg.PlayerArgumentProcessor;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CmdSend extends EnjCommand {

    public CmdSend(EnjCommand parent) {
        super(parent);
        this.aliases.add("send");
        this.requiredArgs.add("player");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_SEND)
                .build();
    }

    @Override
    public List<String> tab(CommandContext context) {
        if (context.args().size() == 1)
            return PlayerArgumentProcessor.INSTANCE.tab(context.sender(), context.args().get(0));

        return new ArrayList<>(0);
    }

    @Override
    public void execute(CommandContext context) {
        Player sender = Objects.requireNonNull(context.player());
        String target = context.args().get(0);

        MetaPlayer senderMetaPlayer = getValidSenderEnjPlayer(context);
        if (senderMetaPlayer == null)
            return;

        Player targetPlayer = getValidTargetPlayer(context, target);
        if (targetPlayer == null)
            return;

        MetaPlayer targetMetaPlayer = getValidTargetEnjPlayer(context, targetPlayer);
        if (targetMetaPlayer == null)
            return;

        ItemStack is = sender.getInventory().getItemInMainHand();
        if (is.getType() == Material.AIR || !is.getType().isItem()) {
            Translation.COMMAND_SEND_MUSTHOLDITEM.send(sender);
            return;
        } else if (!TokenUtils.isValidTokenItem(is)) {
            Translation.COMMAND_SEND_ITEMNOTTOKEN.send(sender);
            return;
        }

        String tokenId = TokenUtils.getTokenID(is);
        String tokenIndex = TokenUtils.getTokenIndex(is);

        MutableBalance balance = senderMetaPlayer.getTokenWallet().getBalance(tokenId, tokenIndex);
        if (balance == null || balance.balance() == 0) {
            Translation.COMMAND_SEND_DOESNOTHAVETOKEN.send(sender);
            return;
        }

        balance.deposit(is.getAmount());
        sender.getInventory().clear(sender.getInventory().getHeldItemSlot());

        if (!senderMetaPlayer.hasEth()) {
            Translation.WALLET_NOTENOUGHETH.send(sender);
            return;
        }

        if (!senderMetaPlayer.hasAllowance()) {
            Translation.WALLET_ALLOWANCENOTSET.send(sender);
            return;
        }

        send(sender, senderMetaPlayer.getIdentityId(), targetMetaPlayer.getIdentityId(), tokenId, tokenIndex, is.getAmount());
    }

    private void send(Player sender, int senderId, int targetId, String tokenId, String tokenIndex, int amount) {
        SendTokenData sendTokenData = tokenIndex == null
                ? SendTokenData.builder()
                .recipientIdentityId(targetId)
                .tokenId(tokenId)
                .value(amount)
                .build()
                : SendTokenData.builder()
                .recipientIdentityId(targetId)
                .tokenId(tokenId)
                .tokenIndex(tokenIndex)
                .value(amount)
                .build();

        TrustedPlatformClient client = bootstrap.getTrustedPlatformClient();
        client.getRequestService().createRequestAsync(new CreateRequest()
                        .appId(client.getAppId())
                        .identityId(senderId)
                        .sendToken(sendTokenData),
                res -> handleNetworkRequest(sender, res));
    }

    private void handleNetworkRequest(CommandSender sender, HttpResponse<GraphQLResponse<Transaction>> res) {
        if (!res.isEmpty()) {
            GraphQLResponse<Transaction> gql = res.body();

            if (gql.hasErrors()) {
                GraphQLException exception = new GraphQLException(gql.getErrors());
                Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                return;
            }

            Translation.COMMAND_SEND_SUBMITTED.send(sender);
        } else if (!res.isSuccess()) {
            NetworkException exception = new NetworkException(res.code());
            Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
            throw exception;
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_SEND_DESCRIPTION;
    }

}
