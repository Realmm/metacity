package org.metacity.metacity.cmd.enj.wallet.trade;

import org.bukkit.entity.Player;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.cmd.MetaCommandWrapper;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;

public class DeclineCmd extends SubCommand<Player> implements MetaCommandWrapper {

    public DeclineCmd() {
        super(Player.class);
        addPermission(Permission.CMD_TRADE_DECLINE.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("decline")));
        addCondition((p, w) -> w.hasNode(2));
        setExecution((p, w) -> {
            String target = w.node(2);

            getValidSenderMetaPlayer(p).ifPresent(m -> {
                getValidTargetPlayer(p, target).ifPresent(t -> {
                    getValidTargetMetaPlayer(p, t).ifPresent(tm -> {
                        try {
                            boolean result = MetaCity.getInstance().getTradeManager().declineInvite(tm, m);
                            if (result) {
                                Translation.COMMAND_TRADE_DECLINED_SENDER.send(p, t.getName());
                                Translation.COMMAND_TRADE_DECLINED_TARGET.send(t, p.getName());
                            } else {
                                Translation.COMMAND_TRADE_NOOPENINVITE.send(p, t.getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
            });
        });
    }

}
