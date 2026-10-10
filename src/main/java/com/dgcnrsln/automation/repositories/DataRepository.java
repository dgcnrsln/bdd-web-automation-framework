package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;

/**
 * {@code testdata} klasöründeki JSON dosyalarından test verisine erişim sağlar.
 */
public class DataRepository {
    private final JsonDataReader reader;

    /**
     * Test verisi klasörünü okuyan bir repository oluşturur.
     */
    public DataRepository() {
        this.reader = new JsonDataReader(ResourcePaths.TEST_DATA);
    }

    /**
     * Anahtara karşılık gelen test verisini döndürür.
     *
     * @param key test verisinin anahtarı
     * @return anahtara ait değer
     * @throws RuntimeException anahtar bulunamazsa
     */
    public String getTestData(String key) {
        return reader.getTestData(key);
    }
}
