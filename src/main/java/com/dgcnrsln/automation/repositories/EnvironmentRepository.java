package com.dgcnrsln.automation.repositories;

import com.dgcnrsln.automation.utils.JsonDataReader;
import com.dgcnrsln.automation.utils.ResourcePaths;

public class EnvironmentRepository {
    private final JsonDataReader reader;

    public EnvironmentRepository() {
        this.reader = new JsonDataReader(ResourcePaths.ENVIRONMENTS);
    }

    public String getEnvironment(String environment) {
        return reader.getTestData(environment);
    }
}
