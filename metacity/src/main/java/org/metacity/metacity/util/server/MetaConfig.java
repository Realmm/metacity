package org.metacity.metacity.util.server;

import org.metacity.metacity.MetaCity;

import java.util.Arrays;
import java.util.List;

public final class MetaConfig {

    static {
        validate();
    }

    public static final boolean DEV_MODE = initBool("dev-mode");
    public static final String WALLET_ADDRESS = initString("eth-wallet-address");
    public static final int APP_ID = initInt("main-app-id");
    public static final String APP_SECRET = initString("main-app-secret");
    public static final int DEV_APP_ID = initInt("dev-app-id");
    public static final String DEV_APP_SECRET = initString("dev-app-secret");
    public static final List<String> PERMISSION_BLACKLIST = getStringList("permission-blacklist");
    public static final List<String> LINK_PERMISSIONS = getStringList("link-permissions");

    public static int getAppId() {
        return DEV_MODE ? DEV_APP_ID : APP_ID;
    }

    public static String getAppSecret() {
        return DEV_MODE ? DEV_APP_SECRET : APP_SECRET;
    }

    private static void validate() {
        Boolean[] array = {
                isBool("dev-mode"),
                isString("eth-wallet-address"),
                isInt("main-app-id"),
                isString("main-app-secret"),
                isInt("dev-app-id"),
                isString("dev-app-secret"),
        };
        if (Arrays.stream(array).anyMatch(b -> !b)) throw new IllegalStateException("MetaCity config.yml incorrectly setup, please correct.");
    }

    private static List<String> getStringList(String path) {
        return MetaCity.getInstance().getConfig().getStringList(path);
    }

    private static boolean isBool(String path) {
        return MetaCity.getInstance().getConfig().isBoolean(path);
    }

    private static boolean isInt(String path) {
        return MetaCity.getInstance().getConfig().isInt(path);
    }

    private static boolean isString(String path) {
        return MetaCity.getInstance().getConfig().isString(path);
    }

    private static boolean initBool(String path) {
        return MetaCity.getInstance().getConfig().getBoolean(path);
    }

    private static int initInt(String path) {
        return MetaCity.getInstance().getConfig().getInt(path);
    }

    private static String initString(String path) {
        return MetaCity.getInstance().getConfig().getString(path);
    }

}
