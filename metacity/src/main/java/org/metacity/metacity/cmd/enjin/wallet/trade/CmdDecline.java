package org.metacity.metacity.cmd.enjin.wallet.trade;

import org.bukkit.entity.Player;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.cmd.enjin.arg.PlayerArgumentProcessor;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CmdDecline extends MetaCommand {

    public CmdDecline(CmdTrade cmdTrade) {
        super(cmdTrade.bootstrap(), cmdTrade);
        this.aliases.add("decline");
        this.requiredArgs.add(CmdTrade.PLAYER_ARG);
        this.requirements = new CommandRequirements.Builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_TRADE_DECLINE)
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

        try {
            boolean result = bootstrap.getTradeManager().declineInvite(targetMetaPlayer, senderMetaPlayer);
            if (result) {
                Translation.COMMAND_TRADE_DECLINED_SENDER.send(sender, targetPlayer.getName());
                Translation.COMMAND_TRADE_DECLINED_TARGET.send(targetPlayer, sender.getName());
            } else {
                Translation.COMMAND_TRADE_NOOPENINVITE.send(sender, targetPlayer.getName());
            }
        } catch (Exception e) {
            bootstrap.log(e);
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TRADE_DECLINE_DESCRIPTION;
    }

}
