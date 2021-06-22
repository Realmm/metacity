package org.metacity.metacity.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper class for a player, resolving online and offline confusion
 */
public class QuestionablePlayer {

    private @Nullable UUID u;

    public QuestionablePlayer(UUID u) {
        this(Bukkit.getOfflinePlayer(u));
    }

    public QuestionablePlayer(Player p) {
        this((OfflinePlayer) p);
    }

    public QuestionablePlayer(OfflinePlayer p) {
        if (p == null || (!p.isOnline() && !p.hasPlayedBefore())) return;
        this.u = p.getUniqueId();
    }

    public QuestionablePlayer(String name) {
        this(Bukkit.getOfflinePlayer(name));
    }

    public QuestionablePlayer(CommandSender sender) {
        if (sender instanceof Player pl) {
            this.u = pl.getUniqueId();
        }
    }

    /**
     * Get the player if they are online
     */
    public Optional<Player> getPlayer() {
        return u == null ? Optional.empty() : Optional.ofNullable(Bukkit.getPlayer(u));
    }

    /**
     * Get the offline player if they are online or have played the server before
     */
    public Optional<OfflinePlayer> getOfflinePlayer() {
        if (u == null) return Optional.empty();
        OfflinePlayer o = Bukkit.getOfflinePlayer(u);
        return !o.hasPlayedBefore() && !o.isOnline() ? Optional.empty() : Optional.of(o);
    }

}
