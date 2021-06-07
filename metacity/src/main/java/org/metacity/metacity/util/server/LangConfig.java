package org.metacity.metacity.util.server;

import org.metacity.metacity.MetaCity;
import org.metacity.util.CC;
import org.metacity.util.YMLFile;

import java.util.Arrays;

public enum LangConfig {

    EN_US("US", "en_US");

    private final String prettyName, configName;
    private final YMLFile config;

    LangConfig(String prettyName, String name) {
        this.prettyName = prettyName;
        this.configName = name;
        this.config = new YMLFile(MetaCity.getInstance(), "lang/" + name);
    }

    public YMLFile config() {
        return config;
    }

    public static LangConfig of(String configName) {
        return Arrays.stream(values()).filter(c -> c.configName.equalsIgnoreCase(configName)).findFirst().orElse(null);
    }

    public String configName() {
        return configName;
    }

    public String prettyName() {
        return CC.YELLOW_200 + prettyName;
    }

}
