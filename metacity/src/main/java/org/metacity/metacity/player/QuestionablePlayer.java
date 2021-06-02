package org.metacity.metacity.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * A wrapper class for a player, resolving online and offline confusion
 */
public class QuestionablePlayer {

    private final UUID u;

    protected QuestionablePlayer(UUID u) {
        this.u = u;
    }

    /**
     * Get the player if they are online
     */
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(u));
    }

    /**
     * Get the offline player if they are online or have played the server before
     */
    public Optional<OfflinePlayer> getOfflinePlayer() {
        OfflinePlayer o = Bukkit.getOfflinePlayer(u);
        return !o.hasPlayedBefore() && !o.isOnline() ? Optional.empty() : Optional.of(o);
    }

}
