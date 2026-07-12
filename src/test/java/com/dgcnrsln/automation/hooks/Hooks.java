package com.dgcnrsln.automation.hooks;

import com.dgcnrsln.automation.drivers.DriverFactory;
import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;

public class Hooks {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setup(Scenario scenario) {
        LOGGER.info("Starting scenario: {}", scenario.getName());
        String browser = resolveBrowser();
        WebDriver driver = DriverFactory.createDriver(browser);
        DriverManager.setDriver(driver);
        LOGGER.info("Driver created for scenario: {}", scenario.getName());
    }

    @AfterStep
    public void attachScreenshotOnFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            LOGGER.warn("Step failed in scenario: {}", scenario.getName());

            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);

            scenario.attach(screenshot, "image/png", "Failure Screenshot");
            LOGGER.info("Screenshot attached for scenario: {}", scenario.getName());
        }
    }

    @After
    public void teardown(Scenario scenario) {
        LOGGER.info("Finished scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());
        DriverManager.quitDriver();
        LOGGER.info("Driver closed for scenario: {}", scenario.getName());
    }

    private String resolveBrowser() {
        String systemProperty = System.getProperty("browser");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String xmlParameter = Reporter.getCurrentTestResult()
                .getTestContext()
                .getCurrentXmlTest()
                .getParameter("browser");

        if (xmlParameter != null && !xmlParameter.isBlank()) {
            return xmlParameter;
        }

        return ConfigReader.get("browser", "chrome");
    }
}
