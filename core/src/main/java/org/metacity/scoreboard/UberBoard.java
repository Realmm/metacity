package org.metacity.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * An UberBoard differs from an {@link MetaScoreboard} as it can handle
 * more that just a side scoreboard, it can also handle name tags above players head and
 * in the tab list. It is also {@link Player} specific, so the boards are intended to be individual to the {@link Player}.
 */
public class UberBoard {

    private String prefix;
    private boolean updateNametag = true, updateTablist = true;
    @Nonnull private final OfflinePlayer p;
    private final MetaScoreboard scoreboard;

    private static final Map<UUID, UberBoard> boardMap = new HashMap<>();

    private UberBoard(@Nonnull OfflinePlayer p, MetaScoreboard scoreboard) {
        this.p = p;
        this.scoreboard = scoreboard;
    }

    /**
     * Create a scoreboard that also manages tablist name color
     * @param p The {@link OfflinePlayer} this scoreboard is to be set to
     * @param scoreboard The {@link MetaScoreboard} to set
     */
    public static UberBoard of(@Nonnull OfflinePlayer p, MetaScoreboard scoreboard) {
        UberBoard board = boardMap.getOrDefault(p.getUniqueId(), new UberBoard(p, scoreboard));
        boardMap.put(p.getUniqueId(), board);
        return board;
    }

    public MetaScoreboard scoreboard() {
        return scoreboard;
    }

    private Team getTeam(OfflinePlayer p) {
        String hash = String.valueOf(p.getUniqueId().hashCode());

        Optional<Team> teamO = scoreboard.getBukkitScoreboard().getTeams()
                .stream()
                .filter(t -> t.getName().equalsIgnoreCase(hash))
                .findFirst();
        return teamO.orElseGet(() -> scoreboard.getBukkitScoreboard().registerNewTeam(hash));
    }

    private String getTabNameColor() {
        return getPrefix().isPresent() ? getPrefix().get() : ChatColor.WHITE.toString();
    }

    private void updateNameTag() {
        if (!p.isOnline()) return;
        Player player = (Player) p;
        Team rankTeam = getTeam(p);
        rankTeam.addEntry(player.getName());

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(o -> !o.getUniqueId().equals(p.getUniqueId()))
                .forEach(o -> {
                    Team team = getTeam(o);
                    UberBoard playerBoard = boardMap.getOrDefault(o.getUniqueId(), null);
                    team.setPrefix(playerBoard == null ? ChatColor.WHITE.toString() : playerBoard.getTabNameColor());
                    team.addEntry(o.getName());
                });
    }

    /**
     * Set the color for the players name above their head and on scoreboard
     * @param color The {@link ChatColor} to color the players name
     */
    public void setColor(ChatColor color) {
        setColors(color);
    }

    /**
     * Set the color for the players name above their head and on scoreboard
     * @param prefix The {@link ChatColor} combination prefix, i.e "&7&c"
     */
    public void setColor(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Set the colors for the players name above their head and on scoreboard
     * This is intended to add the ability to create italic, bold, or any emphasized text besides
     * just a single color
     * @param colors The {@link ChatColor}'s to color the players name
     */
    public void setColors(ChatColor... colors) {
        StringBuilder prefix = new StringBuilder();

        for(ChatColor color : colors) {
            prefix.append(color);
        }

        this.prefix = prefix.toString().trim();
    }

    public Optional<String> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    /**
     * Set whether to update the color above the players head when updating the scoreboard
     * @param state Whether to update the color above the players head
     */
    public void setUpdateNametag(boolean state) {
        this.updateNametag = state;
    }

    /**
     * Get whether to update the players name tag above their head when updating the scoreboard
     * @return True if updating the players name above their head, otherwise false
     */
    public boolean getUpdateNametag() {
        return updateNametag;
    }

    /**
     * Set whether to update the color of the players name on the tab list when updating the scoreboard
     * @param state Whether to update the color of the players name on the tab list
     */
    public void setUpdateTablist(boolean state) {
        this.updateTablist = state;
    }

    /**
     * Get whether to update the players name on the tab list when updating the scoreboard
     * @return True if updating the players name on the tab list, otherwise false
     */
    public boolean getUpdateTablist() {
        return updateTablist;
    }

    /**
     * Updates the scoreboard for the {@link Player} this scoreboard is for
     * Also can update the name tag of the {@link Player} above their head, as well as on the
     * tab list
     */
    public void update() {
        if (!p.isOnline()) return;
        Player player = (Player) p;
        scoreboard.update(player);
        if (updateNametag) {
            updateNameTag();
        }
        if (updateTablist) {
            String color = ChatColor.WHITE.toString();
            if (getPrefix().isPresent()) color = getPrefix().get();
            String tabName = StringUtils.left(color + p.getName(), 16);
            player.setPlayerListName(tabName);
        }
    }

}
