package org.metacity.metacity.cmd.enjin.wallet.trade;


import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.util.server.Translation;

public class CmdTrade extends EnjCommand {

    public static final String PLAYER_ARG = "player";

    public CmdTrade(EnjCommand parent) {
        super(parent);
        this.aliases.add("trade");
        this.requiredArgs.add("action");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(Permission.CMD_TRADE)
                .build();
        this.addSubCommand(new CmdInvite(this));
        this.addSubCommand(new CmdAccept(this));
        this.addSubCommand(new CmdDecline(this));
    }

    @Override
    public void execute(CommandContext context) {
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TRADE_DESCRIPTION;
    }

}
