package org.metacity.metacity.player;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.SpigotBootstrap;
import org.metacity.metacity.events.MetaPlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Listener, PlayerManagerApi {

    private final SpigotBootstrap bootstrap;
    private final Map<UUID, MetaPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager(SpigotBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            MetaPlayer metaPlayer = new MetaPlayer(bootstrap, event.getPlayer());
            addPlayer(metaPlayer);
            // Fetch or create a User and Identity associated with the joining Player
            PlayerInitializationTask.create(bootstrap, metaPlayer);
            metaPlayer.removeQrMap();
            Bukkit.getScheduler().runTaskLater(MetaCity.getInstance(), () -> metaPlayer.board().update(), 20);
        } catch (Exception ex) {
            bootstrap.log(ex);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            MetaPlayer player = removePlayer(event.getPlayer());
            if (player == null)
                return;

            Bukkit.getPluginManager().callEvent(new MetaPlayerQuitEvent(player));
            player.cleanUp();
        } catch (Exception ex) {
            bootstrap.log(ex);
        }
    }

    @Override
    public Map<UUID, MetaPlayer> getPlayers() {
        return new HashMap<>(players);
    }

    @Override
    public Optional<MetaPlayer> getPlayer(@NonNull Player player) throws NullPointerException {
        return getPlayer(player.getUniqueId());
    }

    @Override
    public Optional<MetaPlayer> getPlayer(@NonNull UUID uuid) throws NullPointerException {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public Optional<MetaPlayer> getPlayer(@NonNull String ethAddr) throws NullPointerException {
        return players.values()
                .stream()
                .filter(player -> player.getEthereumAddress() != null && ethAddr.equalsIgnoreCase(player.getEthereumAddress()))
                .findFirst();
    }

    @Override
    public Optional<MetaPlayer> getPlayer(@NonNull Integer identityId) throws NullPointerException {
        return players.values()
                .stream()
                .filter(player -> player.getIdentityId() != null && identityId.equals(player.getIdentityId()))
                .findFirst();
    }

    public void addPlayer(@NonNull MetaPlayer player) throws IllegalArgumentException, NullPointerException {
        if (!player.getBukkitPlayer().isOnline())
            throw new IllegalArgumentException("Player must be online");

        players.put(player.getBukkitPlayer().getUniqueId(), player);
    }

    public MetaPlayer removePlayer(@NonNull Player player) {
        return players.remove(player.getUniqueId());
    }

}
