package org.metacity.metacity.cmd.enj.wallet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.MutableBalance;

import java.math.BigDecimal;

public class BalanceCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public BalanceCmd() {
        super(Player.class);
        addPermission(Permission.CMD_BALANCE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("bal") ||
                s.equalsIgnoreCase("balance")));
        setExecution((p, w) -> {
            getValidSenderMetaPlayer(p).ifPresent(m -> {
                BigDecimal ethBalance = m.getEthBalance() == null
                        ? BigDecimal.ZERO
                        : m.getEthBalance();
                BigDecimal enjBalance = m.getEnjBalance() == null
                        ? BigDecimal.ZERO
                        : m.getEnjBalance();

                Translation.COMMAND_BALANCE_WALLETADDRESS.send(p, m.getEthereumAddress());
                Translation.COMMAND_BALANCE_IDENTITYID.send(p, String.valueOf(m.getIdentityId()));

                if (enjBalance != null)
                    Translation.COMMAND_BALANCE_ENJBALANCE.send(p, enjBalance.toString());
                if (enjBalance != null)
                    Translation.COMMAND_BALANCE_ETHBALANCE.send(p, ethBalance.toString());

                TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

                int itemCount = 0;
                for (MutableBalance balance : m.getTokenWallet().getBalances()) {
                    if (balance.balance() == 0)
                        continue;

                    String fullId;
                    try {
                        fullId = TokenUtils.createFullId(balance.id(), balance.index());
                    } catch (Exception e) {
                        e.printStackTrace();
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
                    Translation.COMMAND_BALANCE_TOKENDISPLAY.send(p, tokenModel.getDisplayName(), balance.balance().toString());
                }

                p.sendMessage("\n");

                if (itemCount == 0)
                    Translation.COMMAND_BALANCE_NOTOKENS.send(p);
                else
                    Translation.COMMAND_BALANCE_TOKENCOUNT.send(p, String.valueOf(itemCount));
            });
        });
    }

}
