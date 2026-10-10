package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.ConfigReader;
import com.dgcnrsln.automation.utils.FailureScreenshot;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Elementler için açık (explicit) beklemeler. Süre, {@code timeout} ayarından okunur.
 * Bekleme başarısız olursa hata aynen yeniden fırlatılmadan önce ekran görüntüsü rapora eklenir.
 */
public class WaitActions {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(ConfigReader.getInt("timeout", 10));

    private WebDriverWait newWait() {
        return new WebDriverWait(DriverManager.getDriver(), DEFAULT_TIMEOUT);
    }

    private <T> T waitFor(ExpectedCondition<T> condition) {
        try {
            return newWait().until(condition);
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }

    /**
     * Element görünür olana kadar bekler.
     *
     * @param locator beklenecek elementin locator'ı
     * @return görünür hale gelen element
     */
    public WebElement visible(By locator) {
        return waitFor(
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
        return waitFor(
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
        return waitFor(
                ExpectedConditions.invisibilityOfElementLocated(locator)
        );
    }
}
