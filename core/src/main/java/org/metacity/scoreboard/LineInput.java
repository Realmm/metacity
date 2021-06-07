package org.metacity.scoreboard;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface LineInput {

    String input(Player p);

}
