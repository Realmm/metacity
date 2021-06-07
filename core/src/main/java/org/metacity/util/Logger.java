package org.metacity.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public final class Logger {

    public static void debug(String msg) {
        log(Level.WARNING, msg);
    }

    public static void error(String msg) {
        log(Level.SEVERE, msg);
    }

    public static void info(String msg) {
        log(Level.INFO, msg);
    }

    public static void log(Level level, String msg) {
        Bukkit.getLogger().log(level, msg);
    }

}
