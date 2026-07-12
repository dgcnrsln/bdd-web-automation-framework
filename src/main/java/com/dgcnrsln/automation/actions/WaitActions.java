package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitActions {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(ConfigReader.getInt("timeout", 10));

    private WebDriverWait newWait() {
        return new WebDriverWait(DriverManager.getDriver(), DEFAULT_TIMEOUT);
    }

    public WebElement visible(By locator) {
        return newWait().until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    public WebElement clickable(By locator) {
        return newWait().until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }

    public boolean invisible(By locator) {
        return newWait().until(
                ExpectedConditions.invisibilityOfElementLocated(locator)
        );
    }
}
