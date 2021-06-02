package org.metacity.metacity.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * The MetaPlayer player wrapper
 * All things individual player related
 */
public class MetaPlayer {

    private final QuestionablePlayer player;

    public MetaPlayer(OfflinePlayer p) {
        player = new QuestionablePlayer(p.getUniqueId());
    }

    public QuestionablePlayer getQuestionablePlayer() {
        return player;
    }

    public OfflinePlayer getOfflinePlayer() {
        return player.getOfflinePlayer().orElse(null);
    }

    public Player getPlayer() {
        return player.getPlayer().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        OfflinePlayer p = null;
        if (o instanceof OfflinePlayer) p = (OfflinePlayer) o;
        if (o instanceof MetaPlayer) p = ((MetaPlayer) o).getOfflinePlayer();
        if (p == null) return false;
        return p.getUniqueId().equals(getOfflinePlayer().getUniqueId());
    }

    public double getBalance() {
        return 1.0;
    }

}

