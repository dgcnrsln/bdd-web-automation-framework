package com.dgcnrsln.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
     * @throws AssertionError   element süre içinde görünür olmazsa
     * @throws RuntimeException doğrulama başka bir WebDriver hatası yüzünden yapılamazsa
     */
    public void verifyElementVisible(By locator) {
        try {
            waitActions.visible(locator);
        } catch (TimeoutException e) {
            throw new AssertionError("Element is not visible. Locator: " + locator, e);
        } catch (WebDriverException e) {
            throw new RuntimeException("Verification could not be performed. Locator: " + locator, e);
        }
    }

    /**
     * Elementin zaman aşımı süresi içinde görünmez olduğunu (veya sayfada bulunmadığını) doğrular.
     *
     * @param locator doğrulanacak elementin locator'ı
     * @throws AssertionError   element süre sonunda hâlâ görünürse
     * @throws RuntimeException doğrulama başka bir WebDriver hatası yüzünden yapılamazsa
     */
    public void verifyElementInvisible(By locator) {
        boolean invisible;

        try {
            invisible = waitActions.invisible(locator);
        } catch (TimeoutException e) {
            throw new AssertionError("Element is still visible. Locator: " + locator, e);
        } catch (WebDriverException e) {
            throw new RuntimeException("Verification could not be performed. Locator: " + locator, e);
        }

        Assert.assertTrue(invisible, "Element is still visible. Locator: " + locator);
    }

    /**
     * Elementin görünen metninin beklenen metni içerdiğini doğrular.
     *
     * @param locator      doğrulanacak elementin locator'ı
     * @param expectedText metnin içermesi beklenen değer
     * @throws AssertionError   element süre içinde görünür olmazsa veya metin beklenen değeri içermiyorsa
     * @throws RuntimeException doğrulama başka bir WebDriver hatası yüzünden yapılamazsa
     */
    public void verifyTextContains(By locator, String expectedText) {
        String actualText;

        try {
            actualText = waitActions.visible(locator).getText();
        } catch (TimeoutException e) {
            throw new AssertionError("Element is not visible. Locator: " + locator, e);
        } catch (WebDriverException e) {
            throw new RuntimeException("Verification could not be performed. Locator: " + locator, e);
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
     * Elementin {@code value} özelliğinin beklenen değere tam olarak eşit olduğunu doğrular.
     *
     * @param locator       doğrulanacak elementin locator'ı
     * @param expectedValue özelliğin eşit olması beklenen değer
     * @throws AssertionError   element süre içinde görünür olmazsa, {@code value} özelliği yoksa veya değer eşit değilse
     * @throws RuntimeException doğrulama başka bir WebDriver hatası yüzünden yapılamazsa
     */
    public void verifyValueEquals(By locator, String expectedValue) {
        String actualValue;

        try {
            actualValue = waitActions.visible(locator).getDomProperty("value");
        } catch (TimeoutException e) {
            throw new AssertionError("Element is not visible. Locator: " + locator, e);
        } catch (WebDriverException e) {
            throw new RuntimeException("Verification could not be performed. Locator: " + locator, e);
        }

        if (actualValue == null) {
            throw new AssertionError(String.format(
                    "Element has no 'value' property. Locator: %s%nExpected value: '%s'",
                    locator,
                    expectedValue
            ));
        }

        Assert.assertEquals(actualValue, expectedValue,
                String.format(
                        "Element: %s%nExpected value: '%s'%nActual value: '%s'",
                        locator,
                        expectedValue,
                        actualValue
                ));
    }
}
