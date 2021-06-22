package org.metacity.metacity.mmo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.metacity.metacity.MetaCity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MMOManager implements Listener {

    public MMOManager() {

    }

    public Optional<MMOPlayer> getPlayer(Player p) {
        return MetaCity.getInstance().getPlayerManager().getPlayer(p).map(m -> (MMOPlayer) m);
    }

    @EventHandler
    public void on(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        getPlayer(p).ifPresent(m -> {
            e.setCancelled(true);
            m.hit(e.getDamage(), true);
        });
    }

}
