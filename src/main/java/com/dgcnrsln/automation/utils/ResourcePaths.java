package com.dgcnrsln.automation.utils;

/**
 * JSON kaynak klasörlerinin proje köküne göre yolları.
 */
public class ResourcePaths {
    private ResourcePaths() {
    }

    /** Element locator dosyalarının klasörü. */
    public static final String LOCATORS = "src/test/resources/locators/";
    /** Test verisi dosyalarının klasörü. */
    public static final String TEST_DATA = "src/test/resources/testdata/";
    /** Ortam (URL) dosyalarının klasörü. */
    public static final String ENVIRONMENTS = "src/test/resources/environments/";
}
