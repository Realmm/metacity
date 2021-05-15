package org.metacity.metacity

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.metacity.core.Core
import org.metacity.core.CorePlugin
import org.metacity.metacity.mmo.AttributeManager
import org.metacity.metacity.player.MetaPlayer

/**
 * MetaCity is a project created by James Andrew, 13th May 2021
 *
 * MetaCity has a goal and an ambition to bring the meta world of crypto to minecraft
 * Collectable NFT's, real world economy, player ran businesses and games,
 * life long blockchain sustained items
 * Backed by the Ethereum Blockchain and powered by the Enjin token
 * Web 3.0 baby...
 */
class MetaCity : CorePlugin() {

    init {
        plugin = this
    }

    fun OfflinePlayer.getMetaPlayer() : MetaPlayer {
        return MetaPlayer(this);
    }

    companion object {

        private lateinit var plugin : MetaCity

        val attributeManager = AttributeManager()

        fun plugin() : MetaCity {
            return plugin
        }

    }

}