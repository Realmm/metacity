package org.metacity.scoreboard;

import org.bukkit.ChatColor;

public enum Slot {

    ONE(ChatColor.AQUA.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 1),
    TWO(ChatColor.YELLOW.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 2),
    THREE(ChatColor.GREEN.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 3),
    FOUR(ChatColor.BOLD.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 4),
    FIVE(ChatColor.GRAY.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 5),
    SIX(ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 6),
    SEVEN(ChatColor.STRIKETHROUGH.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 7),
    EIGHT(ChatColor.RED.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 8),
    NINE(ChatColor.BLUE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 9),
    TEN(ChatColor.BLACK.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 10),
    ELEVEN(ChatColor.DARK_BLUE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 11),
    TWELVE(ChatColor.DARK_GRAY.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 12),
    THIRTEEN(ChatColor.DARK_GREEN.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 13),
    FOURTEEN(ChatColor.DARK_PURPLE.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 14),
    FIFTEEN(ChatColor.DARK_RED.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 15),
    SIXTEEN(ChatColor.GOLD.toString() + ChatColor.YELLOW.toString() + ChatColor.WHITE.toString(), 16);

    final int slot;
    final String id;

    Slot(String id, int slot) {
        this.id = id;
        this.slot = slot;
    }

    public String entry(String input) {
        return id + input;
    }

    public int slot() {
        return slot;
    }

}
