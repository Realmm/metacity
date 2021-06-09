package org.metacity.metacity.cmd.enjin.wallet.trade;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.cmd.enjin.arg.PlayerArgumentProcessor;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CmdInvite extends MetaCommand {

    public CmdInvite(CmdTrade cmdTrade) {
        super(cmdTrade.bootstrap(), cmdTrade);
        this.aliases.add("invite");
        this.requiredArgs.add(CmdTrade.PLAYER_ARG);
        this.requirements = new CommandRequirements.Builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_TRADE_INVITE)
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

        if (BigInteger.ZERO.equals(senderMetaPlayer.getEnjAllowance())) {
            Translation.WALLET_ALLOWANCENOTSET.send(context.sender());
            return;
        }

        invite(senderMetaPlayer, targetMetaPlayer);
    }

    @Override
    protected MetaPlayer getValidTargetEnjPlayer(CommandContext context,
                                                 @NonNull Player targetPlayer) throws NullPointerException {
        CommandSender sender = context.sender();

        MetaPlayer targetMetaPlayer = bootstrap.getPlayerManager()
                .getPlayer(targetPlayer)
                .orElse(null);
        if (targetMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, targetPlayer.getName());
            return null;
        } else if (!targetMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_OTHER.send(sender, targetPlayer.getName());
            Translation.COMMAND_TRADE_WANTSTOTRADE.send(targetPlayer, sender.getName());
            Translation.HINT_LINK.send(targetPlayer);
            return null;
        }

        return targetMetaPlayer;
    }

    private void invite(MetaPlayer sender, MetaPlayer target) {
        throw new UnsupportedOperationException("Unsupported");
//        boolean result = bootstrap.getTradeManager().addInvite(sender, target);
//        if (!result) {
//            Translation.COMMAND_TRADE_ALREADYINVITED.send(sender.getBukkitPlayer(), target.getBukkitPlayer().getName());
//            return;
//        }
//
//        Translation.COMMAND_TRADE_INVITESENT.send(sender.getBukkitPlayer(), target.getBukkitPlayer().getName());
//        Translation.COMMAND_TRADE_INVITEDTOTRADE.send(target.getBukkitPlayer(), sender.getBukkitPlayer().getName());
//        TextComponent.Builder inviteMessageBuilder = Component.text()
//                .append(Component.text("Accept")
//                        .color(NamedTextColor.GREEN)
//                        .clickEvent(ClickEvent.runCommand(String.format("/enj trade accept %s",
//                                sender.getBukkitPlayer().getName()))))
//                .append(Component.text(" | ").color(NamedTextColor.GRAY))
//                .append(Component.text("Decline")
//                        .color(NamedTextColor.RED)
//                        .clickEvent(ClickEvent.runCommand(String.format("/enj trade decline %s",
//                                sender.getBukkitPlayer().getName()))));
//
//        target.getBukkitPlayer().spigot().sendMessage((BaseComponent) inviteMessageBuilder.build()); double check this line
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TRADE_INVITE_DESCRIPTION;
    }

}
