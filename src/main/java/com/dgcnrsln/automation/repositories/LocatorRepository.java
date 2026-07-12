package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;
import org.openqa.selenium.By;

public class LocatorRepository {
    private final JsonDataReader reader;

    public LocatorRepository() {
        this.reader = new JsonDataReader(ResourcePaths.LOCATORS);
    }

    public By getLocator(String elementName) {
        return reader.getLocator(elementName);
    }
}
