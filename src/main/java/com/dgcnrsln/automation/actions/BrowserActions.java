package com.dgcnrsln.automation.actions;

import com.dgcnrsln.automation.drivers.DriverManager;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

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
        File source = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.FILE);

        Path destination = Paths.get(
                "Reports",
                "Screenshots",
                "Screenshot_" + System.currentTimeMillis() + ".png"
        );

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source.toPath(),destination);
        } catch (IOException e) {
            throw new RuntimeException("Screenshots could not be saved.", e);
        }
    }

    /**
     * O an odakta olan elemente Enter tuşunu gönderir.
     */
    public void pressEnter() {
        DriverManager.getDriver()
                .switchTo()
                .activeElement()
                .sendKeys(Keys.ENTER);
    }
}
