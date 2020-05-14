package com.gavel.config;

import com.gavel.utils.SafeProperties;

public class APPConfig {

    private static APPConfig INSTANCE = new APPConfig();

    private APPConfig() {
        init();
    }

    private SafeProperties properties;

    private void init() {
        try {
            properties = SafeProperties.build("config.properties");
            System.out.println("[config.properties]Load 完成");
        } catch (Exception e){

        }
    }

    public static APPConfig getInstance() {
        return INSTANCE;
    }


    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

}
