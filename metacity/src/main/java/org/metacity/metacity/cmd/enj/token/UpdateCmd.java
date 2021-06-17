package org.metacity.metacity.cmd.enj.token;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;
import org.metacity.util.Logger;

import java.util.Objects;

public class UpdateCmd extends SubCommand<Player> {

    public UpdateCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("update")));
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
                fullId = baseModel.isNonfungible()
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

            TokenModel tokenModel = tokenManager.getToken(fullId);
            if (tokenModel == null) {
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                return;
            }

            ItemStack held = p.getInventory().getItemInMainHand();
            if (held.getType() == Material.AIR || !held.getType().isItem()) {
                Translation.COMMAND_TOKEN_NOHELDITEM.send(p);
                return;
            }

            NBTContainer nbt = NBTItem.convertItemtoNBT(held);
            TokenModel newModel = TokenModel.builder()
                    .id(tokenModel.getId())
                    .index(tokenModel.getIndex())
                    .nonfungible(tokenModel.isNonfungible())
                    .alternateId(tokenModel.getAlternateId())
                    .nbt(nbt.toString())
                    .assignablePermissions(tokenModel.getAssignablePermissions())
                    .metadataURI(tokenModel.getMetadataURI())
                    .build();

            int result = tokenManager.updateTokenConf(newModel);
            switch (result) {
                case TokenManager.TOKEN_UPDATE_SUCCESS:
                    Translation.COMMAND_TOKEN_UPDATE_SUCCESS.send(p);
                    break;
                case TokenManager.TOKEN_CREATE_SUCCESS:
                    Translation.COMMAND_TOKEN_CREATE_SUCCESS.send(p);
                    break;
                case TokenManager.TOKEN_CREATE_FAILED:
                    Translation.COMMAND_TOKEN_CREATE_FAILED.send(p);
                    break;
                case TokenManager.TOKEN_UPDATE_FAILED:
                    Translation.COMMAND_TOKEN_UPDATE_FAILED.send(p);
                    break;
                default:
                    Logger.debug(String.format("Unhandled result when updating token (status: %d)", result));
                    break;
            }
        });
    }

}
