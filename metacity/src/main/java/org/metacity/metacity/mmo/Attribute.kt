package org.metacity.metacity.mmo

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.metacity.core.CorePlugin
import org.metacity.metacity.MetaCity

/**
 * Represents a way to keep track of player objectives
 * An Attribute can be if a player breaks a block, or places a certain block, or does a certain thing
 * to achieve a certain task
 */
abstract class Attribute() : Listener {

    init {
        enable()
    }

    /**
     * Disable listening for this attribute
     */
    fun disable() {
        HandlerList.unregisterAll(this)
    }

    /**
     * Enable listening for this attribute
     */
    fun enable() {
        Bukkit.getPluginManager().registerEvents(this, MetaCity.plugin())
    }

}