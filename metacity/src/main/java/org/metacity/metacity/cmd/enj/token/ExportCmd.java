package org.metacity.metacity.cmd.enj.token;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

public class ExportCmd extends SubCommand<ConsoleCommandSender> {

    public ExportCmd() {
        super(ConsoleCommandSender.class);
        addCondition((sender, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("export")));
        setExecution((sender, w) -> {
            String id = w.hasNode(2) ? w.node(2) : null;
            String index = w.hasNode(3) ? w.node(3) : null;

            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

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
                    e.printStackTrace();
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
                    Logger.debug(String.format("Unhandled result when exporting token(s) (status: %d)", result));
                    break;
            }
        });
    }

}
