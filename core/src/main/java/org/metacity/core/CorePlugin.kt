package org.metacity.core

import org.bukkit.plugin.java.JavaPlugin

abstract class CorePlugin : JavaPlugin() {

    init {
        plugin = this
    }

    override fun onEnable() {
        super.onEnable()
        plugin = this
    }

    companion object {

        private lateinit var plugin : CorePlugin

        fun plugin(): CorePlugin {
            return plugin
        }
    }

}