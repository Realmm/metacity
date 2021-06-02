package org.metacity.util;


import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;

import java.awt.*;

/**
 * ChatColor wrapper
 */
public class CC {

    //Standard pallet from material.io
    public static final CC RED_100 = of("#ff8a80");
    public static final CC RED_200 = of("#ff5252");
    public static final CC RED_400 = of("#ff1744");
    public static final CC RED_700 = of("#d50000");
    public static final CC PINK_100 = of("#ff80ab");
    public static final CC PINK_200 = of("#ff4081");
    public static final CC PINK_400 = of("#f50057");
    public static final CC PINK_700 = of("#c51162");
    public static final CC PURPLE_100 = of("#ea80fc");
    public static final CC PURPLE_200 = of("#e040fb");
    public static final CC PURPLE_400 = of("#d500f9");
    public static final CC PURPLE_700 = of("#aa00ff");
    public static final CC DEEP_PURPLE_100 = of("#b388ff");
    public static final CC DEEP_PURPLE_200 = of("#7c4dff");
    public static final CC DEEP_PURPLE_400 = of("#651fff");
    public static final CC DEEP_PURPLE_700 = of("#6200ea");
    public static final CC INDIGO_100 = of("#8c9eff");
    public static final CC INDIGO_200 = of("#536dfe");
    public static final CC INDIGO_400 = of("#3d5afe");
    public static final CC INDIGO_700 = of("#304ffe");
    public static final CC BLUE_100 = of("#82b1ff");
    public static final CC BLUE_200 = of("#448aff");
    public static final CC BLUE_400 = of("#2979ff");
    public static final CC BLUE_700 = of("#2962ff");
    public static final CC LIGHT_BLUE_100 = of("#80d8ff");
    public static final CC LIGHT_BLUE_200 = of("#40c4ff");
    public static final CC LIGHT_BLUE_400 = of("#00b0ff");
    public static final CC LIGHT_BLUE_700 = of("#0091ea");
    public static final CC CYAN_100 = of("#84ffff");
    public static final CC CYAN_200 = of("#18ffff");
    public static final CC CYAN_400 = of("#00e5ff");
    public static final CC CYAN_700 = of("#00b8d4");
    public static final CC TEAL_100 = of("#a7ffeb");
    public static final CC TEAL_200 = of("#64ffda");
    public static final CC TEAL_400 = of("#1de9b6");
    public static final CC TEAL_700 = of("#00bfa5");
    public static final CC GREEN_100 = of("#b9f6ca");
    public static final CC GREEN_200 = of("#69f0ae");
    public static final CC GREEN_400 = of("#00e676");
    public static final CC GREEN_700 = of("#00c853");
    public static final CC LIGHT_GREEN_100 = of("#ccff90");
    public static final CC LIGHT_GREEN_200 = of("#b2ff59");
    public static final CC LIGHT_GREEN_400 = of("#76ff03");
    public static final CC LIGHT_GREEN_700 = of("#64dd17");
    public static final CC LIME_100 = of("#f4ff81");
    public static final CC LIME_200 = of("#eeff41");
    public static final CC LIME_400 = of("#c6ff00");
    public static final CC LIME_700 = of("#aeea00");
    public static final CC YELLOW_100 = of("#ffff8d");
    public static final CC YELLOW_200 = of("#ffff00");
    public static final CC YELLOW_400 = of("#ffea00");
    public static final CC YELLOW_700 = of("#ffd600");
    public static final CC AMBER_100 = of("#ffe57f");
    public static final CC AMBER_200 = of("#ffd740");
    public static final CC AMBER_400 = of("#ffc400");
    public static final CC AMBER_700 = of("#ffab00");
    public static final CC ORANGE_100 = of("#ffd180");
    public static final CC ORANGE_200 = of("#ffab40");
    public static final CC ORANGE_400 = of("#ff9100");
    public static final CC ORANGE_700 = of("#ff6d00");
    public static final CC DEEP_ORANGE_100 = of("#ff9e80");
    public static final CC DEEP_ORANGE_200 = of("#ff6e40");
    public static final CC DEEP_ORANGE_400 = of("#ff3d00");
    public static final CC DEEP_ORANGE_700 = of("#dd2c00");
    public static final CC BROWN_100 = of("#d7ccc8");
    public static final CC BROWN_200 = of("#bcaaa4");
    public static final CC BROWN_400 = of("#8d6e63");
    public static final CC BROWN_700 = of("#5d4037");
    public static final CC GREY_100 = of("#f5f5f5");
    public static final CC GREY_200 = of("#eeeeee");
    public static final CC GREY_400 = of("#bdbdbd");
    public static final CC GREY_700 = of("#616161");
    public static final CC BLUE_GREY_100 = of("#cfd8dc");
    public static final CC BLUE_GREY_200 = of("#b0bec5");
    public static final CC BLUE_GREY_400 = of("#78909c");
    public static final CC BLUE_GREY_700 = of("#455a64");

    //Minecraft Colors
    public static final CC BLACK = of(ChatColor.BLACK);
    public static final CC DARK_BLUE = of(ChatColor.DARK_BLUE);
    public static final CC DARK_GREEN = of(ChatColor.DARK_GREEN);
    public static final CC DARK_AQUA = of(ChatColor.DARK_AQUA);
    public static final CC DARK_RED = of(ChatColor.DARK_RED);
    public static final CC DARK_PURPLE = of(ChatColor.DARK_PURPLE);
    public static final CC GOLD = of(ChatColor.GOLD);
    public static final CC GRAY = of(ChatColor.GRAY);
    public static final CC DARK_GRAY = of(ChatColor.DARK_GRAY);
    public static final CC BLUE = of(ChatColor.BLUE);
    public static final CC GREEN = of(ChatColor.GREEN);
    public static final CC AQUA = of(ChatColor.AQUA);
    public static final CC RED = of(ChatColor.RED);
    public static final CC LIGHT_PURPLE = of(ChatColor.LIGHT_PURPLE);
    public static final CC YELLOW = of(ChatColor.YELLOW);
    public static final CC WHITE = of(ChatColor.WHITE);

    //Minecraft Style
    public static final CC MAGIC = of(ChatColor.MAGIC);
    public static final CC BOLD = of(ChatColor.BOLD);
    public static final CC STRIKETHROUGH = of(ChatColor.STRIKETHROUGH);
    public static final CC UNDERLINE = of(ChatColor.UNDERLINE);
    public static final CC ITALIC = of(ChatColor.ITALIC);
    public static final CC RESET = of(ChatColor.RESET);

    //Minecraft Shortcuts
    public static final CC DBLUE = CC.DARK_BLUE;
    public static final CC DGREEN = CC.DARK_GREEN;
    public static final CC DAQUA = CC.DARK_AQUA;
    public static final CC DRED = CC.DARK_RED;
    public static final CC DPURPLE = CC.DARK_PURPLE;
    public static final CC DGRAY = CC.DARK_GRAY;
    public static final CC LPURPLE = CC.LIGHT_PURPLE;
    public static final CC PINK = CC.LIGHT_PURPLE;
    public static final CC B = CC.BOLD;
    public static final CC U = CC.UNDERLINE;
    public static final CC I = CC.ITALIC;
    public static final CC ITALICS = CC.ITALIC;
    public static final CC R = CC.RESET;
    public static final CC S = CC.STRIKETHROUGH;

    public static CC of(final ChatColor color) {
        return new CC(color);
    }

    public static CC of(final DyeColor color) {
        return of(color.getColor().asRGB());
    }

    public static CC of(final Color color) {
        return of(ChatColor.of(color));
    }

    public static CC of(final String hex) {
        return of(ChatColor.of(hex));
    }

    public static CC of(final int rgb) {
        return of(new Color(rgb));
    }

    @Deprecated
    public static CC fromColor(final DyeColor color) {
        return of(color.getColor().asRGB());
    }

    @Deprecated
    public static CC fromChatColor(final org.bukkit.ChatColor color) {
        return of(color.asBungee());
    }

    public static String strip(final String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(message);
    }

    public static String translate(final char alternate, final String message) {
        return ChatColor.translateAlternateColorCodes(alternate, message);
    }

    public final ChatColor bukkit;
    private final String string;

    private CC(final ChatColor bukkit, final String string){
        this.bukkit = bukkit;
        this.string = string;
    }

    private CC(final ChatColor bukkit) {
        this.bukkit = bukkit;
        this.string = bukkit.toString();
    }

    @Override
    public String toString() {
        return string;
    }

    public String prefix() {
        return this.bold() + "(!) " + this;
    }

    public CC magic() {
        return new CC(bukkit, string + CC.MAGIC);
    }

    public CC bold() {
        return new CC(bukkit, string + CC.BOLD);
    }

    public CC strikethrough() {
        return new CC(bukkit, string + CC.STRIKETHROUGH);
    }

    public CC strike() {
        return this.strikethrough();
    }

    public CC underline() {
        return new CC(bukkit, string + CC.UNDERLINE);
    }

    public CC italic() {
        return new CC(bukkit, string + CC.ITALIC);
    }

    public CC ital() {
        return this.italic();
    }

    public CC italics() {
        return this.italic();
    }

    public CC reset() {
        return new CC(bukkit, CC.R + string);
    }
}
