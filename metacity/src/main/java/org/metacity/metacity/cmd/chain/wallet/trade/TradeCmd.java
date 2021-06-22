package org.metacity.metacity.cmd.chain.wallet.trade;

import org.bukkit.entity.Player;
import org.metacity.commands.Command;

public class TradeCmd extends Command<Player> {

    public TradeCmd() {
        super(Player.class, "trade");
        addSubCommands(
                new AcceptCmd(),
                new DeclineCmd(),
                new InviteCmd()
        );
    }

}
