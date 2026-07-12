package com.dgcnrsln.automation.drivers;

import com.dgcnrsln.automation.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);

    private static final java.util.logging.Logger SELENIUM_DEVTOOLS_LOGGER =
            java.util.logging.Logger.getLogger("org.openqa.selenium.devtools");

    static {
        SELENIUM_DEVTOOLS_LOGGER.setLevel(Level.SEVERE);
    }

    public static WebDriver createDriver(String browser) {
        LOGGER.info("Creating WebDriver instance for browser: {}", browser);

        boolean headless = isHeadless();
        LOGGER.info("Headless mode: {}", headless);

        WebDriver driver = switch (browser.toLowerCase()) {
            case "chrome" -> new ChromeDriver(buildChromeOptions(headless));
            case "firefox" -> new FirefoxDriver(buildFirefoxOptions(headless));
            case "edge" -> new EdgeDriver(buildEdgeOptions(headless));
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        if (!headless) {
            driver.manage().window().maximize();
        }

        return driver;
    }

    private static boolean isHeadless() {
        return ConfigReader.getBoolean("headless", false);
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--window-position=-32000,-32000");
        }

        return options;
    }

    private static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("-headless");
        }

        return options;
    }

    private static EdgeOptions buildEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--window-position=-32000,-32000");
        }

        return options;
    }
}
