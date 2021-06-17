package org.metacity.metacity.cmd.enj.token;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.token.TokenModel;
import org.metacity.metacity.util.server.Translation;

public class GetURICmd extends SubCommand<Player> {

    public GetURICmd() {
        super(Player.class);
        addPermission(Permission.CMD_TOKEN_CREATE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("geturi")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String id = w.node(2);

            TokenModel tokenModel = MetaCity.getInstance().getTokenManager().getToken(id);
            if (tokenModel == null) {
                Translation.COMMAND_TOKEN_NOSUCHTOKEN.send(p);
                return;
            }

            MetaCity.getInstance().chain().getURI(p, tokenModel.getId());
        });
    }

}
