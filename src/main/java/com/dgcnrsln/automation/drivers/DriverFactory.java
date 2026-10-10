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

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Tarayıcı türüne ve yapılandırmaya (headless, sayfa yükleme zaman aşımı) göre WebDriver üretir.
 */
public class DriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);

    private static final java.util.logging.Logger SELENIUM_DEVTOOLS_LOGGER =
            java.util.logging.Logger.getLogger("org.openqa.selenium.devtools");

    static {
        SELENIUM_DEVTOOLS_LOGGER.setLevel(Level.SEVERE);
    }

    /**
     * Verilen tarayıcı için yapılandırılmış yeni bir WebDriver oluşturur.
     * Tarayıcı açıldıktan sonraki ayarlar hata verirse tarayıcı kapatılır ve hata yeniden fırlatılır.
     *
     * @param browser başlatılacak tarayıcı türü
     * @return kullanıma hazır WebDriver
     */
    public static WebDriver createDriver(BrowserType browser) {
        LOGGER.info("Creating WebDriver instance for browser: {}", browser.name().toLowerCase(Locale.ROOT));

        boolean headless = isHeadless();
        LOGGER.info("Headless mode: {}", headless);

        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(buildChromeOptions(headless));
            case FIREFOX -> new FirefoxDriver(buildFirefoxOptions(headless));
            case EDGE -> new EdgeDriver(buildEdgeOptions(headless));
        };

        try {
            int pageLoadTimeout = ConfigReader.getInt("page.load.timeout", 30);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            LOGGER.info("Page load timeout: {}s", pageLoadTimeout);

            if (!headless) {
                driver.manage().window().maximize();
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Driver configuration failed, closing the browser: {}", e.getMessage());
            quitQuietly(driver, e);
            throw e;
        }

        return driver;
    }

    private static void quitQuietly(WebDriver driver, RuntimeException original) {
        try {
            driver.quit();
        } catch (RuntimeException quitError) {
            original.addSuppressed(quitError);
        }
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
