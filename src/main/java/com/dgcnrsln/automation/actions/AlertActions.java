package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.FailureScreenshot;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriverException;

/**
 * Tarayıcı uyarı (alert) pencereleri üzerindeki işlemler.
 */
public class AlertActions {
    /**
     * Açık olan uyarı penceresini onaylar.
     */
    public void accept(){
        try {
            Alert alert = DriverManager.getDriver().switchTo().alert();
            alert.accept();
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }

    /**
     * Açık olan uyarı penceresini reddeder (kapatır).
     */
    public void dismiss(){
        try {
            Alert alert = DriverManager.getDriver().switchTo().alert();
            alert.dismiss();
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }
}
