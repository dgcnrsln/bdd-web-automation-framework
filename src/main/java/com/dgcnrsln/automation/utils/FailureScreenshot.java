package com.dgcnrsln.automation.utils;

import com.dgcnrsln.automation.drivers.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Hata anında ekran görüntüsünü alıp Allure raporuna, o an çalışan adıma ekler.
 * Senaryo başına en fazla bir ek üretir; kendi hatalarını yutar ve yalnızca uyarı loglar.
 */
public final class FailureScreenshot {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailureScreenshot.class);

    private static final String ATTACHMENT_NAME = "Failure Screenshot";

    private static final ThreadLocal<Boolean> ATTACHED = ThreadLocal.withInitial(() -> false);

    private FailureScreenshot() {
    }

    /**
     * Geçerli thread'in driver'ından ekran görüntüsü alıp Allure'a ekler.
     * Bu senaryoda zaten eklendiyse veya driver yoksa hiçbir şey yapmaz; hata fırlatmaz.
     */
    public static void attach() {
        if (ATTACHED.get()) {
            return;
        }

        if (!DriverManager.hasDriver()) {
            LOGGER.warn("No active driver, failure screenshot skipped");
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);

            Allure.addAttachment(ATTACHMENT_NAME, "image/png", new ByteArrayInputStream(screenshot), "png");
            ATTACHED.set(true);
        } catch (RuntimeException e) {
            LOGGER.warn("Failure screenshot could not be taken: {}", e.getMessage());
        }
    }

    /**
     * Geçerli senaryoda ekran görüntüsünün eklenip eklenmediğini bildirir.
     *
     * @return ek üretildiyse {@code true}
     */
    public static boolean isAttached() {
        return ATTACHED.get();
    }

    /**
     * Geçerli thread'in "eklendi" bilgisini sıfırlar. Her senaryonun başında ve sonunda çağrılmalıdır.
     */
    public static void reset() {
        ATTACHED.remove();
    }
}
