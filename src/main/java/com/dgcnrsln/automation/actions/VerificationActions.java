package com.dgcnrsln.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;

/**
 * Sayfa elementleri üzerindeki doğrulamalar (görünürlük, metin, değer).
 */
public class VerificationActions {
    private final WaitActions waitActions;

    /**
     * @param waitActions element beklemeleri için kullanılacak yardımcı
     */
    public VerificationActions(WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    /**
     * Elementin zaman aşımı süresi içinde görünür olduğunu doğrular.
     *
     * @param locator doğrulanacak elementin locator'ı
     * @throws RuntimeException element görünür olmazsa
     */
    public void verifyElementVisible(By locator) {
        try {
            waitActions.visible(locator);
        } catch (WebDriverException e) {
            throw new RuntimeException("Element is not visible. Locator: " + locator, e);
        }
    }

    /**
     * Elementin zaman aşımı süresi içinde görünmez olduğunu (veya sayfada bulunmadığını) doğrular.
     *
     * @param locator doğrulanacak elementin locator'ı
     */
    public void verifyElementInvisible(By locator) {
        boolean invisible = waitActions.invisible(locator);

        Assert.assertTrue(invisible, "Element is still visible. Locator: " + locator);
    }

    /**
     * Elementin görünen metninin beklenen metni içerdiğini doğrular.
     *
     * @param locator      doğrulanacak elementin locator'ı
     * @param expectedText metnin içermesi beklenen değer
     * @throws RuntimeException element görünür olmazsa
     */
    public void verifyTextContains(By locator, String expectedText) {
        String actualText;

        try {
            actualText = waitActions.visible(locator).getText();
        } catch (WebDriverException e) {
            throw new RuntimeException("Element is not visible. Locator: " + locator, e);
        }

        Assert.assertTrue(actualText.contains(expectedText),
                String.format(
                        "Element: %s%nExpected text to contain '%s'%nActual text: '%s'",
                        locator,
                        expectedText,
                        actualText
                ));
    }

    /**
     * Elementin {@code value} özniteliğinin beklenen değeri içerdiğini doğrular.
     *
     * @param locator       doğrulanacak elementin locator'ı
     * @param expectedValue özniteliğin içermesi beklenen değer
     * @throws RuntimeException element görünür olmazsa
     */
    public void verifyValueContains(By locator, String expectedValue) {
        String actualValue;

        try {
            actualValue = waitActions.visible(locator).getAttribute("value");
        } catch (WebDriverException e) {
            throw new RuntimeException("Element is not visible. Locator: " + locator, e);
        }

        Assert.assertTrue(actualValue.contains(expectedValue),
                String.format(
                        "Element: %s%nExpected value to contain '%s'%nActual text: '%s'",
                        locator,
                        expectedValue,
                        actualValue
                ));
    }
}
