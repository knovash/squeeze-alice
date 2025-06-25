package org.knovash.squeezealice.utils;

import org.knovash.squeezealice.Config;

public class ConfigLoader {
    public static Config loadConfig() {
        Config config = new Config();
        config.readConfigProperties();
        config.readConfigJson();
        return config;
    }
}