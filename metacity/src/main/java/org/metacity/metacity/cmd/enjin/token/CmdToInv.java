package org.metacity.metacity.cmd.enjin.token;

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

import java.util.Map;
import java.util.Objects;

public class CmdToInv extends EnjCommand {

    public CmdToInv(EnjCommand parent) {
        super(parent);
        this.aliases.add("toinv");
        this.aliases.add("give");
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

        Map<Integer, ItemStack> leftOver = sender.getInventory().addItem(tokenModel.getItemStack(true));
        if (leftOver.isEmpty())
            Translation.COMMAND_TOKEN_TOINV_SUCCESS.send(sender);
        else
            Translation.COMMAND_TOKEN_TOINV_FAILED.send(sender);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.COMMAND_TOKEN_TOINV_DESCRIPTION;
    }
}
