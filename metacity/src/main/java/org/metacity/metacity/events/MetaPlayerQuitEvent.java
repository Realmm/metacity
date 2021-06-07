package org.metacity.metacity.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.metacity.metacity.player.MetaPlayer;

public class MetaPlayerQuitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final MetaPlayer player;

    public MetaPlayerQuitEvent(MetaPlayer player) {
        this.player = player;
    }

    public MetaPlayer getPlayer() {
        return this.player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
