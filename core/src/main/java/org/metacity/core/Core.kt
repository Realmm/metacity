package org.metacity.core

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Core : JavaPlugin() {

    override fun onEnable() {
        super.onEnable()
        plugin = this
    }

    companion object {
        private var plugin : Plugin? = null

        fun plugin(): Plugin? {
            return plugin
        }
    }

}