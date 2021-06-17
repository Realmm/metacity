package org.metacity.metacity.cmd.enj.perm;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import java.util.List;

public class AddPermNFTCmd extends SubCommand<Player> {

    public AddPermNFTCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_ADDPERM.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("addpermnft")));
        addCondition((p, w) -> w.hasNode(4));
        setExecution((p, w) -> {
            String id = w.node(2);
            String index = w.node(3);
            String perm = w.node(4);
            List<String> worlds = w.nodesFrom(5);

            TokenManager tokenManager = MetaCity.getInstance().getTokenManager();

            TokenModel baseModel = tokenManager.getToken(id);
            if (baseModel != null && !baseModel.isNonfungible()) {
                Translation.COMMAND_TOKEN_ISFUNGIBLE.send(p);
                return;
            } else if (baseModel != null) {
                id = baseModel.getId();
            }

            String fullId;
            try {
                index = TokenUtils.parseIndex(index);
                fullId = TokenUtils.createFullId(id, index);
            } catch (IllegalArgumentException e) {
                Translation.COMMAND_TOKEN_INVALIDFULLID.send(p);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            boolean isGlobal = worlds.isEmpty() || worlds.contains(TokenManager.GLOBAL);
            int result = isGlobal
                    ? tokenManager.addPermissionToToken(perm, fullId, TokenManager.GLOBAL)
                    : tokenManager.addPermissionToToken(perm, fullId, worlds);
            switch (result) {
                case TokenManager.PERM_ADDED_SUCCESS:
                    Translation.COMMAND_TOKEN_ADDPERM_PERMADDED.send(p);
                    break;
                case TokenManager.TOKEN_NOSUCHTOKEN:
                    Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                    break;
                case TokenManager.PERM_ADDED_DUPLICATEPERM:
                    Translation.COMMAND_TOKEN_ADDPERM_DUPLICATEPERM.send(p);
                    break;
                case TokenManager.PERM_ADDED_BLACKLISTED:
                    Translation.COMMAND_TOKEN_ADDPERM_PERMREJECTED.send(p);
                    break;
                case TokenManager.PERM_ISGLOBAL:
                    Translation.COMMAND_TOKEN_PERM_ISGLOBAL.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_UPDATE_FAILED.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when adding non-fungible permission (status: %d)", result));
                    break;
            }
        });
    }

}
