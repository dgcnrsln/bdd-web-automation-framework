package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class ElementActions {
    private final WaitActions waitActions;

    public ElementActions(WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    public void click(By locator) {
        try {
            waitActions.clickable(locator).click();
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be clicked. Locator: " + locator, e);
        }
    }

    public void javascriptClick(By locator) {
        try {
            WebElement element = waitActions.clickable(locator);

            ((JavascriptExecutor) DriverManager.getDriver())
                    .executeScript("arguments[0].click();", element);
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be clicked via JavaScript. Locator: " + locator, e);
        }
    }

    public void type(String text, By locator) {
        try {
            WebElement element = waitActions.visible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (WebDriverException e) {
            throw new RuntimeException("Text could not be entered. Locator: " + locator, e);
        }
    }

    public void clear(By locator) {
        try {
            waitActions.visible(locator).clear();
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be cleared. Locator: " + locator, e);
        }
    }

    public void scroll(By locator) {
        try {
            WebElement element = waitActions.visible(locator);

            ((JavascriptExecutor) DriverManager.getDriver())
                    .executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});",
                            element
                    );
        } catch (WebDriverException e) {
            throw new RuntimeException("Element could not be scrolled into view. Locator: " + locator, e);
        }
    }
}
