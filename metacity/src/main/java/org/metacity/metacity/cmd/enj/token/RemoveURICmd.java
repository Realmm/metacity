package org.metacity.metacity.cmd.enj.token;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

public class RemoveURICmd extends SubCommand<Player> {

    public RemoveURICmd() {
        super(Player.class);
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("removeuri")));
        addCondition((p, w) -> w.hasNode(2));
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        setExecution((p, w) -> {
            String id = w.node(2);

            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();
            TokenModel tokenModel = tokenManager.getToken(id);
            if (tokenModel == null) {
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                return;
            } else if (tokenModel.getMetadataURI().isEmpty()) {
                Translation.COMMAND_TOKEN_REMOVEURI_EMPTY.send(p);
                return;
            }

            int result = tokenManager.updateMetadataURI(tokenModel.getId(), null);
            switch (result) {
                case TokenManager.TOKEN_NOSUCHTOKEN:
                    Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_SUCCESS:
                    Translation.COMMAND_TOKEN_REMOVEURI_SUCCESS.send(p);
                    break;
                case TokenManager.TOKEN_ISNOTBASE:
                    Translation.COMMAND_TOKEN_ISNONFUNGIBLEINSTANCE.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_REMOVEURI_FAILED.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when removing the URI (status: %d)", result));
                    break;
            }
        });

    }

}
