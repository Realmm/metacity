package org.metacity.metacity.player;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.metacity.metacity.MetaCity;
import org.metacity.metacity.events.MetaPlayerQuitEvent;
import org.metacity.scoreboard.MetaScoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Listener, PlayerManagerApi {

    private final Map<UUID, MetaPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager() {

    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void on(PlayerJoinEvent e) {
        if (e.getPlayer().getWorld().getName().equals("world"))
            e.getPlayer().teleport(MetaCity.getInstance().generator().world().getSpawnLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            MetaPlayer metaPlayer = players.getOrDefault(event.getPlayer().getUniqueId(), new MetaPlayer(event.getPlayer()));
            addPlayer(metaPlayer);
            // Fetch or create a User and Identity associated with the joining Player
            PlayerInitializationTask.create(metaPlayer);
            metaPlayer.removeQrMap();
            metaPlayer.board().update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            getPlayer(event.getPlayer()).ifPresent(m ->
                    Bukkit.getPluginManager().callEvent(new MetaPlayerQuitEvent(m)));
        } catch (Exception e) {
            e.printStackTrace();
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
        if (getPlayer(player.uuid()).isPresent()) return;

        players.put(player.uuid(), player);
    }

//    public MetaPlayer removePlayer(@NonNull Player player) {
//        return players.remove(player.getUniqueId());
//    }

}
