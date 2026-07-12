package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;

public class DataRepository {
    private final JsonDataReader reader;

    public DataRepository() {
        this.reader = new JsonDataReader(ResourcePaths.TEST_DATA);
    }

    public String getTestData(String key) {
        return reader.getTestData(key);
    }
}
