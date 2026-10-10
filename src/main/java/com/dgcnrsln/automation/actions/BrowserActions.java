package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import com.dgcnrsln.automation.utils.FailureScreenshot;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Belirli bir elemente bağlı olmayan, tarayıcı düzeyindeki işlemler.
 */
public class BrowserActions {
    /**
     * Ekran görüntüsü alır ve {@code Reports/Screenshots} klasörüne PNG olarak kaydeder.
     *
     * @throws RuntimeException dosya kaydedilemezse
     */
    public void takeScreenshot() {
        File source;

        try {
            source = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.FILE);
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }

        Path destination = Paths.get(
                "Reports",
                "Screenshots",
                "Screenshot_" + System.currentTimeMillis() + ".png"
        );

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source.toPath(),destination);
        } catch (IOException e) {
            FailureScreenshot.attach();
            throw new RuntimeException("Screenshots could not be saved.", e);
        }
    }

    /**
     * O an odakta olan elemente Enter tuşunu gönderir.
     */
    public void pressEnter() {
        try {
            DriverManager.getDriver()
                    .switchTo()
                    .activeElement()
                    .sendKeys(Keys.ENTER);
        } catch (WebDriverException e) {
            FailureScreenshot.attach();
            throw e;
        }
    }
}
