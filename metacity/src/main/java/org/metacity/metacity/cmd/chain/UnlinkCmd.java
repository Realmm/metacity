package org.metacity.metacity.cmd.chain;

import org.bukkit.Bukkit;
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

public class UnlinkCmd extends Command<Player> {

    public UnlinkCmd() {
        super(Player.class, "unlink");
        addPermission(Permission.CMD_UNLINK.node());
        setExecution((p, w) -> {
            Optional<MetaPlayer> om = MetaCity.getInstance().getPlayerManager().getPlayer(p);
            if (!om.isPresent()) {
                Translation.ERRORS_PLAYERNOTREGISTERED.send(p, p.getName());
                return;
            }

            MetaPlayer m = om.get();

            if (!m.isLoaded()) {
                Translation.IDENTITY_NOTLOADED.send(m);
                return;
            }

            if (!m.isLinked()) {
                Translation.WALLET_NOTLINKED_SELF.send(m);
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(MetaCity.getInstance(), () -> {
                try {
                    m.unlink();
                } catch (Exception e) {
                    e.printStackTrace();
                    Translation.ERRORS_EXCEPTION.send(m, e.getMessage());
                }
            });
        });
    }

}
