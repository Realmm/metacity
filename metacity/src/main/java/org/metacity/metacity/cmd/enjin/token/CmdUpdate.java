package org.metacity.metacity.cmd.enjin.token;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.metacity.cmd.enjin.CommandContext;
import org.metacity.metacity.cmd.enjin.CommandRequirements;
import org.metacity.metacity.cmd.enjin.EnjCommand;
import org.metacity.metacity.cmd.enjin.SenderType;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;

public class CmdUpdate extends EnjCommand {

    public CmdUpdate(EnjCommand parent) {
        super(parent);
        this.aliases.add("update");
        this.requiredArgs.add("id");
        this.optionalArgs.add("index");
        this.requirements = CommandRequirements.builder()
                .withPermission(Permission.CMD_TOKEN_CREATE)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        String id = context.args().get(0);
        String index = context.args().size() > requiredArgs.size()
                ? context.args().get(1)
                : null;
        Player sender = Objects.requireNonNull(context.player());

        TokenManager tokenManager = bootstrap.getTokenManager();

        TokenModel baseModel = tokenManager.getToken(id);
        if (baseModel == null) {
            Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(sender);
            return;
        }

        String fullId;
        try {
            fullId = baseModel.isNonfungible()
                    ? TokenUtils.createFullId(baseModel.getId(), TokenUtils.parseIndex(Objects.requireNonNull(index)))
                    : TokenUtils.createFullId(baseModel.getId());
        } catch (NullPointerException e) {
            Translation.COMMAND_TOKEN_MUSTPASSINDEX.send(sender);
            return;
        } catch (IllegalArgumentException e) {
            Translation.COMMAND_TOKEN_INVALIDFULLID.send(sender);
            return;
        } catch (Exception e) {
            bootstrap.log(e);
            return;
        }

        TokenModel tokenModel = tokenManager.getToken(fullId);
        if (tokenModel == null) {
            Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(sender);
            return;
        }

        ItemStack held = sender.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR || !held.getType().isItem()) {
            Translation.COMMAND_TOKEN_NOHELDITEM.send(sender);
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
                Translation.COMMAND_TOKEN_UPDATE_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_SUCCESS:
                Translation.COMMAND_TOKEN_CREATE_SUCCESS.send(sender);
                break;
            case TokenManager.TOKEN_CREATE_FAILED:
                Translation.COMMAND_TOKEN_CREATE_FAILED.send(sender);
                break;
            case TokenManager.TOKEN_UPDATE_FAILED:
                Translation.COMMAND_TOKEN_UPDATE_FAILED.send(sender);
                break;
            default:
                bootstrap.debug(String.format("Unhandled result when updating token (status: %d)", result));
                break;
        }
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_UPDATE_DESCRIPTION;
    }

}
