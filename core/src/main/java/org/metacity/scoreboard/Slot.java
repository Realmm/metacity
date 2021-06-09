package org.metacity.scoreboard;

import org.bukkit.ChatColor;

import java.util.List;

public enum Slot implements Comparable<Slot> {

    ONE(ChatColor.AQUA.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 15),
    TWO(ChatColor.YELLOW.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 14),
    THREE(ChatColor.GREEN.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 13),
    FOUR(ChatColor.BOLD.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 12),
    FIVE(ChatColor.GRAY.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 11),
    SIX(ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 10),
    SEVEN(ChatColor.STRIKETHROUGH.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 9),
    EIGHT(ChatColor.RED.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 8),
    NINE(ChatColor.BLUE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 7),
    TEN(ChatColor.BLACK.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 6),
    ELEVEN(ChatColor.DARK_BLUE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 5),
    TWELVE(ChatColor.DARK_GRAY.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 4),
    THIRTEEN(ChatColor.DARK_GREEN.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 3),
    FOURTEEN(ChatColor.DARK_PURPLE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 2),
    FIFTEEN(ChatColor.DARK_RED.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 1),
    SIXTEEN(ChatColor.GOLD.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 0);

    final int slot;
    final String id;

    Slot(String id, int slot) {
        this.id = id;
        this.slot = slot;
    }

    public String entry() {
        return id;
    }

    public int slot() {
        return slot;
    }

}
