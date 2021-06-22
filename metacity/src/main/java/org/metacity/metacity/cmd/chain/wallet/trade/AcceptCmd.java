package org.metacity.metacity.cmd.chain.wallet.trade;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.util.server.Translation;

public class AcceptCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public AcceptCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TRADE_ACCEPT.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("accept")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String target = w.node(2);

            getValidSenderMetaPlayer(p).ifPresent(m -> {
                getValidTargetPlayer(p, target).ifPresent(t -> {
                    getValidTargetMetaPlayer(p, t).ifPresent(tm -> {
                        try {
                            boolean result = MetaCity.getInstance().getTradeManager().acceptInvite(tm, m);
                            if (!result)
                                Translation.COMMAND_TRADE_NOOPENINVITE.send(p, t.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
            });
        });


    }

}
