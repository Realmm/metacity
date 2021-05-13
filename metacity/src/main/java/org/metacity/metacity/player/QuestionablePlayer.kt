package org.metacity.metacity.player

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * A wrapper class for a player, resolving online and offline confusion
 */
class QuestionablePlayer(val u : UUID) {

    /**
     * Get the player if they are online
     */
    fun getPlayer() : Optional<Player> {
        return Optional.ofNullable(Bukkit.getPlayer(u));
    }

    /**
     * Get the offline player if they are online or have played the server before
     */
    fun getOfflinePlayer() : Optional<OfflinePlayer> {
        val o = Bukkit.getOfflinePlayer(u);
        return if (!o.hasPlayedBefore() && !o.isOnline) Optional.empty() else Optional.of(o);
    }

}