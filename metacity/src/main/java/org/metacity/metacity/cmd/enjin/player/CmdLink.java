package org.metacity.metacity.cmd.enjin.player;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.StringUtils;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;

public class CmdLink extends EnjCommand {

    public CmdLink(EnjCommand parent) {
        super(parent);
        this.aliases.add("link");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_LINK)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        MetaPlayer senderMetaPlayer = getValidSenderEnjPlayer(context);
        if (senderMetaPlayer != null && senderMetaPlayer.isLinked())
            existingLink(context.sender(), senderMetaPlayer.getEthereumAddress());
        else if (senderMetaPlayer != null)
            linkInstructions(context.sender(), senderMetaPlayer.getLinkingCode());
    }

    @Override
    protected MetaPlayer getValidSenderEnjPlayer(@NonNull CommandContext context) throws NullPointerException {
        Player sender = Objects.requireNonNull(context.player(), "Expected context to have non-null player as sender");

        MetaPlayer senderMetaPlayer = context.enjinPlayer();
        if (senderMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, sender.getName());
            return null;
        } else if (!senderMetaPlayer.isLoaded()) {
            Translation.IDENTITY_NOTLOADED.send(sender);
            return null;
        }

        return senderMetaPlayer;
    }

    private void existingLink(CommandSender sender, String address) {
        if (StringUtils.isEmpty(address))
            Translation.COMMAND_LINK_NULLWALLET.send(sender);
        else
            Translation.COMMAND_LINK_SHOWWALLET.send(sender, address);
    }

    private void linkInstructions(CommandSender sender, String code) {
        if (StringUtils.isEmpty(code)) {
            Translation.COMMAND_LINK_NULLCODE.send(sender);
        } else {
            Translation.COMMAND_LINK_INSTRUCTIONS_1.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_2.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_3.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_4.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_5.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_6.send(sender);
            Translation.COMMAND_LINK_INSTRUCTIONS_7.send(sender, code);
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_LINK_DESCRIPTION;
    }

}
