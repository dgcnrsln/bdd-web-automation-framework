package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.FailureScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tarayıcı gezinme işlemleri (adrese gitme, yenileme, geri, ileri).
 */
public class NavigationActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationActions.class);

    /**
     * Yeni bir gezinme yardımcısı oluşturur.
     */
    public NavigationActions() {
    }

    /**
     * Verilen adrese gider.
     *
     * @param url açılacak adres
     */
    public void goTo(String url) {
        LOGGER.debug("Navigating to: {}", url);

        try {
            DriverManager.getDriver().get(url);
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }

    /**
     * Geçerli sayfayı yeniler.
     */
    public void refresh() {
        LOGGER.debug("Refreshing the page");

        try {
            DriverManager.getDriver()
                    .navigate()
                    .refresh();
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }

    /**
     * Tarayıcı geçmişinde bir önceki sayfaya döner.
     */
    public void back() {
        LOGGER.debug("Navigating back");

        try {
            DriverManager.getDriver()
                    .navigate()
                    .back();
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }

    /**
     * Tarayıcı geçmişinde bir sonraki sayfaya ilerler.
     */
    public void forward() {
        LOGGER.debug("Navigating forward");

        try {
            DriverManager.getDriver()
                    .navigate()
                    .forward();
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }
}
