package org.metacity.metacity.cmd.enj.wallet;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.wallet.TokenWalletView;

import java.util.Objects;

public class WalletCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public WalletCmd() {
        super(Player.class);
        addPermission(Permission.CMD_WALLET.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("wallet") ||
                s.equalsIgnoreCase("wal")));
        setExecution((p, w) -> {
            getValidSenderMetaPlayer(p).ifPresent(m -> new TokenWalletView(m).open(p));
        });
    }

}
