package org.metacity.metacity.cmd.enjin.wallet;

import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;
import org.metacity.metacity.wallet.TokenWalletView;

import java.util.Objects;

public class CmdWallet extends EnjCommand {

    public CmdWallet(EnjCommand parent) {
        super(parent);
        this.aliases.add("wallet");
        this.aliases.add("wal");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_WALLET)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Player sender = Objects.requireNonNull(context.player());

        MetaPlayer senderMetaPlayer = getValidSenderEnjPlayer(context);
        if (senderMetaPlayer == null)
            return;

        new TokenWalletView(bootstrap, senderMetaPlayer).open(sender);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_WALLET_DESCRIPTION;
    }

}
