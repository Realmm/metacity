package org.metacity.metacity.cmd.enj.token;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenManager;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.server.Translation;

public class CreateCmd extends SubCommand<Player> {

    public CreateCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("create")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            ItemStack held = p.getInventory().getItemInMainHand();

            // Ensure player is holding a valid item
            if (held.getType() == Material.AIR || !held.getType().isItem()) {
                Translation.COMMAND_TOKEN_NOHELDITEM.send(p);
                return;
            }

            String id = w.node(2);
            MetaCity.getInstance().getPlayerManager().getPlayer(p).ifPresent(m -> {
                TokenManager tm = MetaCity.getInstance().getTokenManager();
                TokenModel model = tm.getToken(id);
                MetaCity.getInstance().chain().startConversation(m, held, model == null ? id : model.getId());
            });
        });
    }

}
