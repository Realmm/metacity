package org.metacity.util;

import org.bukkit.ChatColor;

import java.util.LinkedList;
import java.util.List;

public final class Util {

    public static String color(String s) {
        return CC.translate('&', s);
    }

    /**
     * This method is confusing but works... change later.
     * @param string The string to translate
     * @param length The length of the string
     * @return The length of the translated string
     */
    @Deprecated
    private static int translateLength(String string, int length) {
        int nonColorCharCount = 0;
        boolean previousWasColorChar = false;
        for (int i = 0; i < string.length(); i++)
            if (previousWasColorChar)
                previousWasColorChar = false;
            else if (string.charAt(i) == ChatColor.COLOR_CHAR)
                previousWasColorChar = true;
            else {
                nonColorCharCount++;
                if (nonColorCharCount == length)
                    return i + 1;
            }
        return string.length();
    }

    /**
     * Wraps the given String, but avoids cutting color characters off.
     *
     * @param string     The String to wrap.
     * @param lineLength The length of each line.
     * @return A list of wrapped text.
     */
    public static List<String> wrapWithColor(String string, int lineLength) {
        int length = translateLength(string, lineLength);
        List<String> lines;
        if (length == string.length()) {
            lines = new LinkedList<>();
            lines.add(string);
        } else {
            int lastSpace = string.lastIndexOf(' ', length);
            length = lastSpace == -1 ? length : lastSpace + 1;
            String line = string.substring(0, length).trim();
            lines = wrapWithColor(ChatColor.getLastColors(line) + string.substring(length).trim(), lineLength);
            lines.add(0, line);
        }
        return lines;
    }

}
