package org.metacity.metacity.cmd.enjin.token;


import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.cmd.enjin.perm.CmdAddPerm;
import org.metacity.metacity.cmd.enjin.perm.CmdAddPermNFT;
import org.metacity.metacity.cmd.enjin.perm.CmdRevokePerm;
import org.metacity.metacity.cmd.enjin.perm.CmdRevokePermNFT;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.util.server.Translation;

public class CmdToken extends MetaCommand {

    public CmdToken(MetaCommand parent) {
        super(parent);
        this.aliases.add("token");
        this.requiredArgs.add("operation");
        this.requirements = CommandRequirements.builder()
                .withPermission(Permission.CMD_TOKEN)
                .withAllowedSenderTypes(SenderType.PLAYER, SenderType.CONSOLE)
                .build();
        this.subCommands.add(new CmdCreate(this));
        this.subCommands.add(new CmdUpdate(this));
        this.subCommands.add(new CmdDelete(this));
        this.subCommands.add(new CmdToInv(this));
        this.subCommands.add(new CmdNickname(this));
        this.subCommands.add(new CmdAddPerm(this));
        this.subCommands.add(new CmdAddPermNFT(this));
        this.subCommands.add(new CmdRevokePerm(this));
        this.subCommands.add(new CmdRevokePermNFT(this));
        this.subCommands.add(new CmdSetWalletView(this));
        this.subCommands.add(new CmdList(this));
        this.subCommands.add(new CmdExport(this));
        this.subCommands.add(new CmdImport(this));
    }

    @Override
    public void execute(CommandContext context) {
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_DESCRIPTION;
    }

}
