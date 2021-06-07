package org.metacity.metacity.cmd.enjin.wallet.send;

import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.models.request.CreateRequest;
import com.enjin.sdk.models.request.Transaction;
import com.enjin.sdk.models.request.data.SendTokenData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.cmd.enjin.arg.PlayerArgumentProcessor;
import org.metacity.metacity.cmd.enjin.arg.TokenDefinitionArgumentProcessor;
import org.metacity.metacity.exceptions.GraphQLException;
import org.metacity.metacity.exceptions.NetworkException;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.PlayerUtils;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.MetaConfig;
import org.metacity.metacity.util.server.Translation;

import java.util.ArrayList;
import java.util.List;

public class CmdDevSend extends EnjCommand {

    public static final int    ETH_ADDRESS_LENGTH = 42;
    public static final String ETH_ADDRESS_PREFIX = "0x";

    public CmdDevSend(EnjCommand parent) {
        super(parent);
        this.aliases.add("devsend");
        this.requiredArgs.add("player|address");
        this.requiredArgs.add("id");
        this.requiredArgs.add("index|amount");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.CONSOLE)
                .build();
    }

    @Override
    public List<String> tab(CommandContext context) {
        if (context.args().size() == 1)
            return PlayerArgumentProcessor.INSTANCE.tab(context.sender(), context.args().get(0));
        else if (context.args().size() == 2)
            return TokenDefinitionArgumentProcessor.INSTANCE.tab(context.sender(), context.args().get(1));

        return new ArrayList<>(0);
    }

    @Override
    public void execute(CommandContext context) {
        String target = context.args().get(0);
        String id = context.args().get(1);

        // Process target address
        String targetAddr;
        if (target.startsWith(ETH_ADDRESS_PREFIX) && target.length() == ETH_ADDRESS_LENGTH) {
            targetAddr = target;
        } else if (PlayerUtils.isValidUserName(target)) {
            Player targetPlayer = getValidTargetPlayer(context, target);
            if (targetPlayer == null)
                return;

            MetaPlayer targetMetaPlayer = getValidTargetEnjPlayer(context, targetPlayer);
            if (targetMetaPlayer == null)
                return;

            targetAddr = targetMetaPlayer.getEthereumAddress();
        } else {
            Translation.ERRORS_INVALIDPLAYERNAME.send(context.sender(), target);
            return;
        }

        TokenModel tokenModel = bootstrap.getTokenManager().getToken(id);
        if (tokenModel == null) {
            Translation.COMMAND_DEVSEND_INVALIDTOKEN.send(context.sender());
            return;
        }

        // Process send data
        SendTokenData data;
        if (tokenModel.isNonfungible()) { // Non-fungible token
            try {
                String index = TokenUtils.parseIndex(context.args().get(2));
                data = SendTokenData.builder()
                        .recipientAddress(targetAddr)
                        .tokenId(tokenModel.getId())
                        .tokenIndex(index)
                        .build();
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_TOKEN_INVALIDFULLID.send(context.sender());
                return;
            } catch (Exception e) {
                bootstrap.log(e);
                return;
            }
        } else { // Fungible token
            try {
                Integer amount = context.argToInt(2).orElse(null);
                if (amount == null || amount <= 0)
                    throw new IllegalArgumentException("Invalid amount to send");

                data = SendTokenData.builder()
                        .recipientAddress(targetAddr)
                        .tokenId(tokenModel.getId())
                        .value(amount)
                        .build();
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_DEVSEND_INVALIDAMOUNT.send(context.sender());
                return;
            } catch (Exception e) {
                bootstrap.log(e);
                return;
            }
        }

        send(context.sender(), MetaConfig.WALLET_ADDRESS, data);
    }

    private void send(CommandSender sender, String senderAddress, SendTokenData data) {
        TrustedPlatformClient client = bootstrap.getTrustedPlatformClient();
        client.getRequestService().createRequestAsync(new CreateRequest()
                        .appId(client.getAppId())
                        .ethAddr(senderAddress)
                        .sendToken(data),
                networkResponse -> {
                    if (!networkResponse.isSuccess()) {
                        NetworkException exception = new NetworkException(networkResponse.code());
                        Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                        throw exception;
                    }

                    GraphQLResponse<Transaction> graphQLResponse = networkResponse.body();
                    if (!graphQLResponse.isSuccess()) {
                        GraphQLException exception = new GraphQLException(graphQLResponse.getErrors());
                        Translation.ERRORS_EXCEPTION.send(sender, exception.getMessage());
                        throw exception;
                    }

                    Translation.COMMAND_SEND_SUBMITTED.send(sender);
        });
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_DEVSEND_DESCRIPTION;
    }

}
