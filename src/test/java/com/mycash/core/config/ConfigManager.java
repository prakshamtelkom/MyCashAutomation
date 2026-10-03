//package com.mycash.core.config;
//
//import java.io.InputStream;
//import java.util.Properties;
//
//public class ConfigManager {
//
//    private static final Properties properties = new Properties();
//
//    static {
//        loadConfig();
//    }
//
//    public static void loadConfig() {
//        try {
//            InputStream inputStream =
//                    ConfigManager.class.getClassLoader().getResourceAsStream("qa.properties");
//
//            if (inputStream == null) {
//                throw new RuntimeException("qa.properties NOT FOUND in src/test/resources");
//            }
//
//            properties.load(inputStream);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to load qa.properties", e);
//        }
//    }
//
//    public static String getProperty(String key) {
//        String value = properties.getProperty(key);
//        return value != null ? value.trim() : null;
//    }
//}