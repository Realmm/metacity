package org.metacity.metacity.cmd.enj.token;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import java.util.Objects;

public class DeleteCmd extends SubCommand<Player> {

    public DeleteCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("delete")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String id = w.node(2);
            String index = w.hasNode(3) ? w.node(3) : null;

            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            TokenModel baseModel = tokenManager.getToken(id);
            if (baseModel == null) {
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                return;
            }

            String fullId;
            try {
                fullId = baseModel.isNonfungible() && index != null
                        ? TokenUtils.createFullId(baseModel.getId(), TokenUtils.parseIndex(Objects.requireNonNull(index)))
                        : TokenUtils.createFullId(baseModel.getId());
            } catch (NullPointerException e) {
                Translation.COMMAND_TOKEN_MUSTPASSINDEX.send(p);
                return;
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_TOKEN_INVALIDFULLID.send(p);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            int result = tokenManager.deleteTokenConf(fullId);
            switch (result) {
                case TokenManager.TOKEN_DELETE_SUCCESS:
                    Translation.COMMAND_TOKEN_DELETE_SUCCESS.send(p);
                    return;
                case TokenManager.TOKEN_DELETE_FAILED:
                    Translation.COMMAND_TOKEN_DELETE_FAILED.send(p);
                    return;
                case TokenManager.TOKEN_DELETE_FAILEDNFTBASE:
                    Translation.COMMAND_TOKEN_DELETE_BASENFT_1.send(p);
                    Translation.COMMAND_TOKEN_DELETE_BASENFT_2.send(p);
                    return;
                default:
                    Logger.debug(String.format("Unhandled result when deleting token (status: %d)", result));
                    break;
            }
        });
    }

}
