package org.metacity.metacity.player;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManagerApi {

    Map<UUID, MetaPlayer> getPlayers();

    Optional<MetaPlayer> getPlayer(Player player);

    Optional<MetaPlayer> getPlayer(UUID uuid);

    Optional<MetaPlayer> getPlayer(String ethAddr);

    Optional<MetaPlayer> getPlayer(Integer identityId);

}
