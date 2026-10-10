package com.dgcnrsln.automation.drivers;

import org.openqa.selenium.WebDriver;

/**
 * Her thread'e özel WebDriver örneğini tutar; paralel koşumda oturumların karışmasını önler.
 */
public class DriverManager {
    private DriverManager() {
    }

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    /**
     * Geçerli thread'e ait WebDriver'ı döndürür.
     *
     * @return geçerli thread'in WebDriver'ı
     * @throws IllegalStateException bu thread için driver oluşturulmamışsa
     */
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

    /**
     * Geçerli thread için bir WebDriver olup olmadığını bildirir.
     *
     * @return driver varsa {@code true}
     */
    public static boolean hasDriver() {
        return driver.get() != null;
    }

    /**
     * Geçerli thread'in WebDriver'ını ayarlar.
     *
     * @param driver bu thread'de kullanılacak WebDriver
     */
    public static void setDriver(WebDriver driver) {
        DriverManager.driver.set(driver);
    }

    /**
     * Geçerli thread'in WebDriver'ını kapatır ve kaydını siler.
     * Kapatma sırasında hata olsa bile kayıt temizlenir.
     */
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
