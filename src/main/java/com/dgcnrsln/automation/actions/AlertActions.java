package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import org.openqa.selenium.Alert;

/**
 * Tarayıcı uyarı (alert) pencereleri üzerindeki işlemler.
 */
public class AlertActions {
    /**
     * Açık olan uyarı penceresini onaylar.
     */
    public void accept(){
        Alert alert = DriverManager.getDriver().switchTo().alert();
        alert.accept();
    }

    /**
     * Açık olan uyarı penceresini reddeder (kapatır).
     */
    public void dismiss(){
        Alert alert = DriverManager.getDriver().switchTo().alert();
        alert.dismiss();
    }
}
