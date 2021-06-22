package org.metacity.metacity.cmd.chain.wallet;

import org.bukkit.entity.Player;
import org.metacity.commands.Command;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.wallet.TokenWalletView;

public class WalletCmd extends Command<Player> implements MetaCommandWrapper {

    public WalletCmd() {
        super(Player.class, "wallet");
        addPermission(Permission.CMD_WALLET.node());
        setExecution((p, w) -> {
            getValidSenderMetaPlayer(p).ifPresent(m -> new TokenWalletView(m).open(p));
        });
    }

}
