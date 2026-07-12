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

public class BrowserActions {
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

    public void pressEnter() {
        DriverManager.getDriver()
                .switchTo()
                .activeElement()
                .sendKeys(Keys.ENTER);
    }
}
