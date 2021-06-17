package org.metacity.metacity.cmd.enj.token;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

public class ImportCmd extends SubCommand<ConsoleCommandSender> {

    public ImportCmd() {
        super(ConsoleCommandSender.class);
        addCondition((sender, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("import")));
        setExecution((sender, w) -> {
            int result = MetaCity.getInstance().getTokenManager().importTokens();
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
                    Logger.debug(String.format("Unhandled result when importing token(s) (status: %d)", result));
                    break;
            }
        });
    }

}
