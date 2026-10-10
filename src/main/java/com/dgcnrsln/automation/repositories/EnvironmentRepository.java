package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;

/**
 * {@code environments} klasöründeki JSON dosyalarından ortam adreslerine (URL) erişim sağlar.
 */
public class EnvironmentRepository {
    private final JsonDataReader reader;

    /**
     * Ortam klasörünü okuyan bir repository oluşturur.
     */
    public EnvironmentRepository() {
        this.reader = new JsonDataReader(ResourcePaths.ENVIRONMENTS);
    }

    /**
     * Ortam adına karşılık gelen adresi döndürür.
     *
     * @param environment ortamın adı (JSON'daki anahtar)
     * @return ortama ait URL
     * @throws RuntimeException ortam bulunamazsa
     */
    public String getEnvironment(String environment) {
        return reader.getTestData(environment);
    }
}
