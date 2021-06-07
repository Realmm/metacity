package org.metacity.metacity.cmd.enjin.token;

import org.bukkit.command.CommandSender;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

public class CmdExport extends EnjCommand {

    public CmdExport(EnjCommand parent) {
        super(parent);
        this.aliases.add("export");
        this.optionalArgs.add("id");
        this.optionalArgs.add("index");
        this.requirements = CommandRequirements.builder()
                .withAllowedSenderTypes(SenderType.CONSOLE)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        String id = context.args().size() > 0
                ? context.args().get(0)
                : null;
        String index = context.args().size() > 1
                ? context.args().get(1)
                : null;
        CommandSender sender = context.sender();

        TokenManager tokenManager = bootstrap.getTokenManager();

        int result;
        if (id != null && index != null) {
            TokenModel baseModel = tokenManager.getToken(id);
            if (baseModel != null)
                id = baseModel.getId();

            String fullId;
            try {
                fullId = TokenUtils.createFullId(id, TokenUtils.parseIndex(index));
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_TOKEN_INVALIDFULLID.send(sender);
                return;
            } catch (Exception e) {
                Translation.ERRORS_EXCEPTION.send(sender, e.toString());
                bootstrap.log(e);
                return;
            }

            result = tokenManager.exportToken(fullId);
        } else if (id != null) {
            result = tokenManager.exportToken(id);
        } else {
            result = tokenManager.exportTokens();
        }

        switch (result) {
            case TokenManager.TOKEN_EXPORT_SUCCESS:
                Translation.COMMAND_TOKEN_EXPORT_COMPLETE.send(sender);
                Translation.COMMAND_TOKEN_EXPORT_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_EXPORT_PARTIAL:
                Translation.COMMAND_TOKEN_EXPORT_COMPLETE.send(sender);
                Translation.COMMAND_TOKEN_EXPORT_PARTIAL.send(sender);
                break;
            case TokenManager.TOKEN_NOSUCHTOKEN:
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(sender);
                break;
            case TokenManager.TOKEN_EXPORT_EMPTY:
                Translation.COMMAND_TOKEN_EXPORT_EMPTY.send(sender);
                break;
            case TokenManager.TOKEN_EXPORT_FAILED:
                Translation.COMMAND_TOKEN_EXPORT_FAILED.send(sender);
                break;
            default:
                bootstrap.debug(String.format("Unhandled result when exporting token(s) (status: %d)", result));
                break;
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_EXPORT_DESCRIPTION;
    }

}
