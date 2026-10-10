package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;
import org.openqa.selenium.By;

/**
 * {@code locators} klasöründeki JSON dosyalarından element locator'larına erişim sağlar.
 */
public class LocatorRepository {
    private final JsonDataReader reader;

    /**
     * Locator klasörünü okuyan bir repository oluşturur.
     */
    public LocatorRepository() {
        this.reader = new JsonDataReader(ResourcePaths.LOCATORS);
    }

    /**
     * Element adına karşılık gelen locator'ı döndürür.
     *
     * @param elementName elementin JSON'daki adı
     * @return elemente ait Selenium locator'ı
     * @throws RuntimeException element adı bulunamazsa
     */
    public By getLocator(String elementName) {
        return reader.getLocator(elementName);
    }
}
