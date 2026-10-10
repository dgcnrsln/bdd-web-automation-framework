package com.dgcnrsln.automation.drivers;

import org.openqa.selenium.WebDriver;

public class DriverManager {
    private DriverManager() {
    }

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        WebDriver current = driver.get();

        if (current == null) {
            throw new IllegalStateException(
                    "WebDriver has not been initialized for the current thread. "
                            + "Make sure DriverManager.setDriver() was called (see the @Before hook) before using the driver."
            );
        }

        return current;
    }

    public static boolean hasDriver() {
        return driver.get() != null;
    }

    public static void setDriver(WebDriver driver) {
        DriverManager.driver.set(driver);
    }

    public static void quitDriver() {
        WebDriver current = driver.get();

        if (current != null) {
            try {
                current.quit();
            } finally {
                driver.remove();
            }
        }
    }
}
