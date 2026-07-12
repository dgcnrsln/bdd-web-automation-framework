package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import org.openqa.selenium.Alert;

public class AlertActions {
    public void accept(){
        Alert alert = DriverManager.getDriver().switchTo().alert();
        alert.accept();
    }

    public void dismiss(){
        Alert alert = DriverManager.getDriver().switchTo().alert();
        alert.dismiss();
    }
}
