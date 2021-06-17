package org.metacity.metacity.cmd.enj.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.commands.Command;
import org.metacity.commands.SubCommand;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.enums.Permission;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;
import java.util.Optional;

public class LinkCmd extends SubCommand<Player> {

    public LinkCmd() {
        super(Player.class);
        addPermission(Permission.CMD_LINK.node());
        addCondition((p, w) -> w.validateNode(1, s -> s.equalsIgnoreCase("link")));
        setExecution((p, w) -> {
            Optional<MetaPlayer> mo = MetaCity.getInstance().getPlayerManager().getPlayer(p);
            if (!mo.isPresent()) {
                Translation.ERRORS_PLAYERNOTREGISTERED.send(p, p.getName());
                return;
            }

            MetaPlayer m = mo.get();
            if (!m.isLoaded()) {
                Translation.IDENTITY_NOTLOADED.send(m);
                return;
            }

            if (m.isLinked()) {
                if (m.getEthereumAddress().isEmpty()) Translation.COMMAND_LINK_NULLWALLET.send(m);
                else Translation.COMMAND_LINK_SHOWWALLET.send(m, m.getEthereumAddress());
            } else {
                if (m.getLinkingCode().isEmpty()) {
                    Translation.COMMAND_LINK_NULLCODE.send(m);
                } else {
                    Translation.COMMAND_LINK_INSTRUCTIONS_1.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_2.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_3.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_4.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_5.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_6.send(m);
                    Translation.COMMAND_LINK_INSTRUCTIONS_7.send(m, m.getLinkingCode());
                }
            }
        });
    }

}
