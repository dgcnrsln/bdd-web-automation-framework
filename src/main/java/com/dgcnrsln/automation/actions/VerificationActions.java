package com.dgcnrsln.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;

public class VerificationActions {
    private final WaitActions waitActions;

    public VerificationActions(WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    public void verifyElementVisible(By locator) {
        try {
            waitActions.visible(locator);
        } catch (WebDriverException e) {
            throw new RuntimeException("Element is not visible. Locator: " + locator, e);
        }
    }

    public void verifyElementInvisible(By locator) {
        boolean invisible = waitActions.invisible(locator);

        Assert.assertTrue(invisible, "Element is still visible. Locator: " + locator);
    }

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
