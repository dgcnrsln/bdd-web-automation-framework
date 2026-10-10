package com.dgcnrsln.automation.drivers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Framework'ün desteklediği tarayıcı türleri.
 */
public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE;

    /**
     * Metin olarak verilen tarayıcı adını (büyük/küçük harf duyarsız) enum değerine çevirir.
     *
     * @param value tarayıcı adı, örn. "chrome"
     * @return eşleşen tarayıcı türü
     * @throws IllegalArgumentException değer boşsa veya desteklenmeyen bir tarayıcıysa
     */
    public static BrowserType fromString(String value) {
        if (value != null) {
            String normalized = value.trim().toUpperCase(Locale.ROOT);

            for (BrowserType type : values()) {
                if (type.name().equals(normalized)) {
                    return type;
                }
            }
        }

        throw new IllegalArgumentException(
                "Unsupported browser: '" + value + "'. Supported browsers: " + supportedValues()
        );
    }

    private static String supportedValues() {
        return Arrays.stream(values())
                .map(type -> type.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
}
