package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Elementler için açık (explicit) beklemeler. Süre, {@code timeout} ayarından okunur.
 */
public class WaitActions {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(ConfigReader.getInt("timeout", 10));

    private WebDriverWait newWait() {
        return new WebDriverWait(DriverManager.getDriver(), DEFAULT_TIMEOUT);
    }

    /**
     * Element görünür olana kadar bekler.
     *
     * @param locator beklenecek elementin locator'ı
     * @return görünür hale gelen element
     */
    public WebElement visible(By locator) {
        return newWait().until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    /**
     * Element tıklanabilir olana kadar bekler.
     *
     * @param locator beklenecek elementin locator'ı
     * @return tıklanabilir hale gelen element
     */
    public WebElement clickable(By locator) {
        return newWait().until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }

    /**
     * Element görünmez olana (veya sayfadan kalkana) kadar bekler.
     *
     * @param locator beklenecek elementin locator'ı
     * @return element görünmez olduysa {@code true}
     */
    public boolean invisible(By locator) {
        return newWait().until(
                ExpectedConditions.invisibilityOfElementLocated(locator)
        );
    }
}
