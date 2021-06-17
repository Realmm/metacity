package org.metacity.metacity.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

class PlayerListener implements Listener {

    private final MetaPlayer p;

    private PlayerListener(MetaPlayer p) {
        this.p = p;
    }

    static PlayerListener of(MetaPlayer p) {
        return new PlayerListener(p);
    }

    @EventHandler
    public void onPlayerWorldChanged(PlayerChangedWorldEvent event) {
        p.player().ifPresent(p -> {
            if (event.getPlayer() != p) return;

            this.p.setWorldAttachment(p.getWorld().getName());
        });
    }

}
