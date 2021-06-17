package org.metacity.metacity.cmd.enj.perm;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import java.util.List;

public class RevokePermCmd extends SubCommand<Player> {

    public RevokePermCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_REVOKEPERM.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("revokeperm")));
        addCondition((p, w) -> w.hasNode(3));
        setExecution((p, w) -> {
            String id = w.node(2);
            String perm = w.node(3);
            List<String> worlds = w.nodesFrom(4);

            boolean isGlobal = worlds.isEmpty() || worlds.contains(TokenManager.GLOBAL);
            int result = isGlobal
                    ? MetaCity.getInstance().getTokenManager().removePermissionFromToken(perm, id, TokenManager.GLOBAL)
                    : MetaCity.getInstance().getTokenManager().removePermissionFromToken(perm, id, worlds);
            switch (result) {
                case TokenManager.PERM_REMOVED_SUCCESS:
                    Translation.COMMAND_TOKEN_REVOKEPERM_PERMREVOKED.send(p);
                    break;
                case TokenManager.TOKEN_NOSUCHTOKEN:
                    Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                    break;
                case TokenManager.PERM_REMOVED_NOPERMONTOKEN:
                    Translation.COMMAND_TOKEN_REVOKEPERM_PERMNOTONTOKEN.send(p);
                    break;
                case TokenManager.PERM_ISGLOBAL:
                    Translation.COMMAND_TOKEN_PERM_ISGLOBAL.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_UPDATE_FAILED.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when removing base permission (status: %d)", result));
                    break;
            }
        });
    }

}
