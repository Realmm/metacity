package org.metacity.metacity.cmd.enjin.wallet;

import org.bukkit.command.CommandSender;
import org.metacity.metacity.cmd.enjin.CmdMeta;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;

import java.math.BigDecimal;

public class CmdBalance extends MetaCommand {

    public CmdBalance(CmdMeta parent) {
        super(parent);
        this.aliases.add("balance");
        this.aliases.add("bal");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_BALANCE)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        CommandSender sender = context.sender();

        MetaPlayer metaPlayer = getValidSenderEnjPlayer(context);
        if (metaPlayer == null)
            return;

        BigDecimal ethBalance = metaPlayer.getEthBalance() == null
                ? BigDecimal.ZERO
                : metaPlayer.getEthBalance();
        BigDecimal enjBalance = metaPlayer.getEnjBalance() == null
                ? BigDecimal.ZERO
                : metaPlayer.getEnjBalance();

        Translation.COMMAND_BALANCE_WALLETADDRESS.send(sender, metaPlayer.getEthereumAddress());
        Translation.COMMAND_BALANCE_IDENTITYID.send(sender, String.valueOf(metaPlayer.getIdentityId()));

        if (enjBalance != null)
            Translation.COMMAND_BALANCE_ENJBALANCE.send(sender, enjBalance.toString());
        if (enjBalance != null)
            Translation.COMMAND_BALANCE_ETHBALANCE.send(sender, ethBalance.toString());

        TokenManager tokenManager = bootstrap.getTokenManager();

        int itemCount = 0;
        for (MutableBalance balance : metaPlayer.getTokenWallet().getBalances()) {
            if (balance.balance() == 0)
                continue;

            String fullId;
            try {
                fullId = TokenUtils.createFullId(balance.id(), balance.index());
            } catch (Exception e) {
                bootstrap.log(e);
                continue;
            }

            TokenModel tokenModel = tokenManager.getToken(fullId);
            if (tokenModel == null) {
                // Try fetching base token in case this balance is an NFT with no mapping for the given index.
                tokenModel = tokenManager.getToken(TokenUtils.createFullId(balance.id()));
                if (tokenModel == null) {
                    continue;
                }
            }

            ++itemCount;
            Translation.COMMAND_BALANCE_TOKENDISPLAY.send(sender, tokenModel.getDisplayName(), balance.balance().toString());
        }

        sender.sendMessage("\n");

        if (itemCount == 0)
            Translation.COMMAND_BALANCE_NOTOKENS.send(sender);
        else
            Translation.COMMAND_BALANCE_TOKENCOUNT.send(sender, String.valueOf(itemCount));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_BALANCE_DESCRIPTION;
    }

}
