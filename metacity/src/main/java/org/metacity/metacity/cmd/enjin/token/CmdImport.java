package org.metacity.metacity.cmd.enjin.token;

import org.bukkit.command.CommandSender;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.MetaCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.util.server.Translation;

public class CmdImport extends MetaCommand {

    public CmdImport(MetaCommand parent) {
        super(parent);
        this.aliases.add("import");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.CONSOLE)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        CommandSender sender = context.sender();

        int result = bootstrap.getTokenManager().importTokens();
        switch (result) {
            case TokenManager.TOKEN_IMPORT_SUCCESS:
                Translation.COMMAND_TOKEN_IMPORT_COMPLETE.send(sender);
                Translation.COMMAND_TOKEN_IMPORT_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_IMPORT_PARTIAL:
                Translation.COMMAND_TOKEN_IMPORT_COMPLETE.send(sender);
                Translation.COMMAND_TOKEN_IMPORT_PARTIAL.send(sender);
                break;
            case TokenManager.TOKEN_IMPORT_EMPTY:
                Translation.COMMAND_TOKEN_IMPORT_EMPTY.send(sender);
                break;
            case TokenManager.TOKEN_IMPORT_FAILED:
                Translation.COMMAND_TOKEN_IMPORT_FAILED.send(sender);
                break;
            default:
                bootstrap.debug(String.format("Unhandled result when importing token(s) (status: %d)", result));
                break;
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_IMPORT_DESCRIPTION;
    }

}
