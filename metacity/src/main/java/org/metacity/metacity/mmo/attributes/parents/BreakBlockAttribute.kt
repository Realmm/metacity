package org.metacity.metacity.mmo.attributes.parents

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.metacity.metacity.mmo.Attribute

class BreakBlockAttribute : Attribute() {

    @EventHandler
    fun on(e: BlockBreakEvent) {

    }

}