package com.mycash.core.config;

import org.testng.annotations.BeforeSuite;

public class TestConfigInitializer {

    @BeforeSuite(alwaysRun = true)
    public void initConfig() {
        // Adjust path if your config file is elsewhere
        String configPath = "qa.properties";
        ConfigReader.load(configPath);
        System.out.println(" ConfigReader initialized with: " + configPath);
    }
}