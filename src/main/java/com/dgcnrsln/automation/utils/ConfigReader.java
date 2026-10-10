package com.dgcnrsln.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Yapılandırma değerlerini okur. Öncelik sırası: JVM sistem property'si ({@code -D}),
 * {@code config.properties}, kod içi varsayılan.
 */
public class ConfigReader {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "config.properties was not found on the classpath (expected under src/test/resources)."
                );
            }

            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private ConfigReader() {
    }

    /**
     * Anahtarın değerini metin olarak döndürür.
     *
     * @param key          ayar anahtarı
     * @param defaultValue hiçbir kaynakta bulunamazsa kullanılacak değer
     * @return ayarın değeri
     */
    public static String get(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        return PROPERTIES.getProperty(key, defaultValue);
    }

    /**
     * Anahtarın değerini boolean olarak döndürür.
     *
     * @param key          ayar anahtarı
     * @param defaultValue hiçbir kaynakta bulunamazsa kullanılacak değer
     * @return değer "true" ise {@code true}, aksi halde {@code false}
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    /**
     * Anahtarın değerini tam sayı olarak döndürür.
     *
     * @param key          ayar anahtarı
     * @param defaultValue hiçbir kaynakta bulunamazsa kullanılacak değer
     * @return ayarın tam sayı değeri
     * @throws NumberFormatException değer tam sayıya çevrilemezse
     */
    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }
}
