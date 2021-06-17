package org.metacity.metacity.cmd.enj.token;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.TokenUtils;
import org.metacity.metacity.util.server.Translation;

import java.util.Map;
import java.util.Objects;

public class GiveCmd extends SubCommand<Player> {

    public GiveCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("give") ||
                s.equalsIgnoreCase("toinv")));
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

            Map<Integer, ItemStack> leftOver = p.getInventory().addItem(tokenModel.getItemStack(true));
            if (leftOver.isEmpty())
                Translation.COMMAND_TOKEN_TOINV_SUCCESS.send(p);
            else
                Translation.COMMAND_TOKEN_TOINV_FAILED.send(p);
        });
    }

}
