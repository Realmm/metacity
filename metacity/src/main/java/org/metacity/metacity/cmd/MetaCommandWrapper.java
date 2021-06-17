package org.metacity.metacity.cmd;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.player.MetaPlayer;
import org.metacity.metacity.player.QuestionablePlayer;
import org.metacity.metacity.util.server.Translation;

import java.util.Objects;
import java.util.Optional;

public interface MetaCommandWrapper {

    default Optional<MetaPlayer> getValidSenderMetaPlayer(Player p) throws NullPointerException {
        Player sender = Objects.requireNonNull(p, "Expected context to have non-null player as sender");
        Optional<MetaPlayer> o = MetaCity.getInstance().getPlayerManager().getPlayer(p);
        if (!o.isPresent()) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, sender.getName());
            return Optional.empty();
        }

        MetaPlayer m = o.get();
        if (!m.isLinked()) {
            Translation.WALLET_NOTLINKED_SELF.send(sender);
            return Optional.empty();
        }

        return Optional.of(m);
    }

    default Optional<Player> getValidTargetPlayer(@NonNull CommandSender sender, @NonNull String targetName) {
        QuestionablePlayer q = new QuestionablePlayer(targetName);
        if (!q.getPlayer().isPresent()) {
            Translation.ERRORS_PLAYERNOTONLINE.send(sender, targetName);
            return Optional.empty();
        }
        Player target = q.getPlayer().get();

        Optional<Player> o = new QuestionablePlayer(sender).getPlayer();
        if (o.isPresent()) {
            Player s = o.get();
            if (s.equals(target)) {
                Translation.ERRORS_CHOOSEOTHERPLAYER.send(sender);
                return Optional.empty();
            }
        }

        return q.getPlayer();
    }

    default Optional<MetaPlayer> getValidTargetMetaPlayer(@NonNull CommandSender sender,
                                                  @NonNull Player targetPlayer) throws NullPointerException {
        MetaPlayer targetMetaPlayer = MetaCity.getInstance().getPlayerManager()
                .getPlayer(targetPlayer)
                .orElse(null);
        if (targetMetaPlayer == null) {
            Translation.ERRORS_PLAYERNOTREGISTERED.send(sender, targetPlayer.getName());
            return Optional.empty();
        } else if (!targetMetaPlayer.isLinked()) {
            Translation.WALLET_NOTLINKED_OTHER.send(sender, targetPlayer.getName());
            return Optional.empty();
        }

        return Optional.of(targetMetaPlayer);
    }

}
