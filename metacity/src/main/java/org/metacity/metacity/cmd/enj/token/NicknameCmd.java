package org.metacity.metacity.cmd.enj.token;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

public class NicknameCmd extends SubCommand<Player> {

    public NicknameCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("nickname")));
        addCondition((p, w) -> w.hasNode(3));
        setExecution((p, w) -> {
            String id = w.node(2);
            String alternateId = w.node(3);

            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            TokenModel tokenModel = tokenManager.getToken(id);
            if (tokenModel == null) {
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                return;
            } else if (tokenModel.isNonfungible()) {
                id = TokenUtils.normalizeFullId(tokenModel.getFullId());
            } else {
                id = tokenModel.getFullId();
            }

            int result = tokenManager.updateAlternateId(id, alternateId);
            switch (result) {
                case TokenManager.TOKEN_UPDATE_SUCCESS:
                    Translation.COMMAND_TOKEN_NICKNAME_SUCCESS.send(p);
                    break;
                case TokenManager.TOKEN_NOSUCHTOKEN:
                    Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                    break;
                case TokenManager.TOKEN_DUPLICATENICKNAME:
                    Translation.COMMAND_TOKEN_NICKNAME_DUPLICATE.send(p);
                    break;
                case TokenManager.TOKEN_HASNICKNAME:
                    Translation.COMMAND_TOKEN_NICKNAME_HAS.send(p);
                    break;
                case TokenManager.TOKEN_INVALIDNICKNAME:
                    Translation.COMMAND_TOKEN_NICKNAME_INVALID.send(p);
                    break;
                case TokenManager.TOKEN_ISNOTBASE:
                    Translation.COMMAND_TOKEN_ISNONFUNGIBLEINSTANCE.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_UPDATE_FAILED.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when setting nickname (status: %d)", result));
                    break;
            }
        });
    }

}
