package org.metacity.metacity.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.metacity.metacity.MetaCity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PlayerManager implements Listener {

    private final List<MetaPlayer> players = new ArrayList<>();

    @EventHandler
    public void on(PlayerJoinEvent e) {
        registerPlayer(e.getPlayer());
    }

    public Optional<MetaPlayer> getMetaPlayer(OfflinePlayer p) {
        return players.stream().filter(m -> m.equals(p)).findFirst();
    }

    private boolean isRegistered(OfflinePlayer p) {
        return players.stream().anyMatch(m -> m.equals(p));
    }

    private void registerPlayer(OfflinePlayer p) {
        if (isRegistered(p)) return;
        players.add(new MetaPlayer(p));
        MetaCity.getInstance().getEnjin().authPlayer(p);
    }

}
