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

public class CmdAccept extends MetaCommand {

    public CmdAccept(CmdTrade cmdTrade) {
        super(cmdTrade.bootstrap(), cmdTrade);
        this.aliases.add("accept");
        this.requiredArgs.add(CmdTrade.PLAYER_ARG);
        this.requirements = new CommandRequirements.Builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_TRADE_ACCEPT)
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

        try {
            boolean result = bootstrap.getTradeManager().acceptInvite(targetMetaPlayer, senderMetaPlayer);
            if (!result)
                Translation.COMMAND_TRADE_NOOPENINVITE.send(context.sender(), targetPlayer.getName());
        } catch (Exception e) {
            bootstrap.log(e);
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TRADE_ACCEPT_DESCRIPTION;
    }

}
