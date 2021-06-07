package org.metacity.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An UberBoard differs from an {@link RealmScoreboard} as it can handle
 * more that just a side scoreboard, it can also handle name tags above players head and
 * in the tab list. It is also {@link Player} specific, so the boards are intended to be individual to the {@link Player}.
 */
public class UberBoard extends RealmScoreboard {

    private String prefix;
    private boolean updateNametag = true, updateTablist = true;
    private final Player p;

    private static final Map<UUID, UberBoard> boardMap = new HashMap<>();

    /**
     * Create a scoreboard with a certain title
     * @param p The {@link Player} this scoreboard is to be set to
     * @param title The title to set
     */
    private UberBoard(Player p, String title) {
        super(title);
        this.p = p;
    }

    /**
     * Create a scoreboard with a certain {@link LineExecution} title
     * @param p The {@link Player} this scoreboard is to be set to
     * @param executed The player to execute the {@link LineExecution}
     * @param title The {@link LineExecution} title to set
     */
    private UberBoard(Player p, Player executed, LineExecution title) {
        super(executed, title);
        this.p = p;
    }

    /**
     * Create a scoreboard with a certain title and {@link LineExecution}'s
     * @param p The {@link Player} this scoreboard is to be set to
     * @param title The title to set
     * @param lines The {@link LineExecution}'s to set
     */
    private UberBoard(Player p, String title, LineExecution... lines) {
        super(title, lines);
        this.p = p;
    }

    /**
     * Create a scoreboard with a certain title and a list of {@link LineExecution}'s
     * @param p The {@link Player} this scoreboard is to be set to
     * @param title The title to set
     * @param lines The {@link LineExecution}'s to set
     */
    private UberBoard(Player p, String title, List<LineExecution> lines) {
        super(title, lines);
        this.p = p;
    }

    public static UberBoard of(Player p, String title) {
        UberBoard board = boardMap.getOrDefault(p.getUniqueId(), new UberBoard(p, title));
        boardMap.put(p.getUniqueId(), board);
        return board;
    }

    public static UberBoard of(Player p, Player executed, LineExecution title) {
        UberBoard board = boardMap.getOrDefault(p.getUniqueId(), new UberBoard(p, executed, title));
        boardMap.put(p.getUniqueId(), board);
        return board;
    }

    public static UberBoard of(Player p, String title, LineExecution... lines) {
        UberBoard board = boardMap.getOrDefault(p.getUniqueId(), new UberBoard(p, title, lines));
        boardMap.put(p.getUniqueId(), board);
        return board;
    }

    public static UberBoard of(Player p, String title, List<LineExecution> lines) {
        UberBoard board = boardMap.getOrDefault(p.getUniqueId(), new UberBoard(p, title, lines));
        boardMap.put(p.getUniqueId(), board);
        return board;
    }

    private Team getTeam(Player p) {
        String hash = String.valueOf(p.getUniqueId().hashCode());

        Optional<Team> teamO = getBukkitScoreboard().getTeams()
                .stream()
                .filter(t -> t.getName().equalsIgnoreCase(hash))
                .findFirst();
        return teamO.orElseGet(() -> getBukkitScoreboard().registerNewTeam(hash));
    }

    private String getTabNameColor() {
        return getPrefix().isPresent() ? getPrefix().get() : ChatColor.WHITE.toString();
    }

    private void updateNameTag() {
        Team rankTeam = getTeam(p);
        rankTeam.addEntry(p.getName());

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

        return prefix == null ? Optional.empty() : Optional.of(prefix);
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
        super.update(p);
        if (updateNametag) {
            updateNameTag();
        }
        if (updateTablist) {
            String color = ChatColor.WHITE.toString();
            if (getPrefix().isPresent()) color = getPrefix().get();
            String tabName = StringUtils.left(color + p.getName(), 16);
            p.setPlayerListName(tabName);
        }
    }

}
