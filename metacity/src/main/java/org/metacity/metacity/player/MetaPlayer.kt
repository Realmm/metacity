package org.metacity.metacity.player

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * The MetaPlayer player wrapper
 * All things individual player related
 */
class MetaPlayer(private val p : OfflinePlayer) {

    fun getQuestionablePlayer() : QuestionablePlayer {
        return QuestionablePlayer(p.uniqueId)
    }

    fun getOfflinePlayer() : OfflinePlayer? {
        return getQuestionablePlayer().getOfflinePlayer().orElse(null)
    }

    fun getPlayer() : Player? {
        return getQuestionablePlayer().getPlayer().orElse(null)
    }

    fun getBalance() : Double {
        return 1.0
    }

}