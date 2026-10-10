package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
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

        DriverManager.getDriver().get(url);
    }

    /**
     * Geçerli sayfayı yeniler.
     */
    public void refresh() {
        LOGGER.debug("Refreshing the page");

        DriverManager.getDriver()
                .navigate()
                .refresh();
    }

    /**
     * Tarayıcı geçmişinde bir önceki sayfaya döner.
     */
    public void back() {
        LOGGER.debug("Navigating back");

        DriverManager.getDriver()
                .navigate()
                .back();
    }

    /**
     * Tarayıcı geçmişinde bir sonraki sayfaya ilerler.
     */
    public void forward() {
        LOGGER.debug("Navigating forward");

        DriverManager.getDriver()
                .navigate()
                .forward();
    }
}
