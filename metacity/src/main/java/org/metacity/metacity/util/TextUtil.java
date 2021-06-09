package org.metacity.metacity.util;

import org.bukkit.ChatColor;

import java.util.List;

public class TextUtil {

    private TextUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String concat(List<String> list, String glue) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                builder.append(glue);
            builder.append(list.get(i));
        }

        return builder.toString();
    }

}
