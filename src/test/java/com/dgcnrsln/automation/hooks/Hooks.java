package com.dgcnrsln.automation.hooks;

import com.dgcnrsln.automation.drivers.BrowserType;
import com.dgcnrsln.automation.drivers.DriverFactory;
import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.ConfigReader;
import com.dgcnrsln.automation.utils.FailureScreenshot;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setup(Scenario scenario) {
        LOGGER.info("Starting scenario: {}", scenario.getName());
        FailureScreenshot.reset();
        BrowserType browser = BrowserType.fromString(resolveBrowser());
        WebDriver driver = DriverFactory.createDriver(browser);
        DriverManager.setDriver(driver);
        LOGGER.info("Driver created for scenario: {}", scenario.getName());
    }

    @After
    public void teardown(Scenario scenario) {
        LOGGER.info("Finished scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());

        try {
            if (scenario.isFailed() && !FailureScreenshot.isAttached()) {
                LOGGER.warn("No failure screenshot yet for scenario: {} - attaching one in teardown", scenario.getName());
                FailureScreenshot.attach();
            }
        } finally {
            FailureScreenshot.reset();
            DriverManager.quitDriver();
        }

        LOGGER.info("Driver closed for scenario: {}", scenario.getName());
    }

    private String resolveBrowser() {
        return ConfigReader.get("browser", "chrome");
    }
}
