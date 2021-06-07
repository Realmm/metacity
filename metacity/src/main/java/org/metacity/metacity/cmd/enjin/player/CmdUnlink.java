package org.metacity.metacity.cmd.enjin.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;

public class CmdUnlink extends EnjCommand {

    public CmdUnlink(EnjCommand parent) {
        super(parent);
        this.aliases.add("unlink");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_UNLINK)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        MetaPlayer senderMetaPlayer = getValidSenderEnjPlayer(context);
        if (senderMetaPlayer == null)
            return;

        Bukkit.getScheduler().runTaskAsynchronously(bootstrap.plugin(), () -> {
            try {
                senderMetaPlayer.unlink();
            } catch (Exception ex) {
                bootstrap.log(ex);
                Translation.ERRORS_EXCEPTION.send(context.sender(), ex.getMessage());
            }
        });
    }

    @Override
    protected MetaPlayer getValidSenderEnjPlayer(CommandContext context) {
        Player sender = Objects.requireNonNull(context.player(), "Expected context to have non-null player as sender");

        MetaPlayer senderMetaPlayer = context.enjinPlayer();
        if (senderMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, sender.getName());
            return null;
        } else if (!senderMetaPlayer.isLoaded()) {
            Translation.IDENTITY_NOTLOADED.send(sender);
            return null;
        } else if (!senderMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_SELF.send(sender);
            return null;
        }

        return senderMetaPlayer;
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_UNLINK_DESCRIPTION;
    }

}
