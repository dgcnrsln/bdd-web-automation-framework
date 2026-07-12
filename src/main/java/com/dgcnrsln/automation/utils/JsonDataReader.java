package com.dgcnrsln.automation.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonDataReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, Map<String, By>> LOCATOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> DATA_CACHE = new ConcurrentHashMap<>();

    private final Map<String, By> locatorMap;
    private final Map<String, String> testDataMap;

    public JsonDataReader(String jsonFolderPath) {
        this.locatorMap = LOCATOR_CACHE.computeIfAbsent(jsonFolderPath, path -> loadLocators(path));
        this.testDataMap = DATA_CACHE.computeIfAbsent(jsonFolderPath, path -> loadTestData(path));
    }

    private Map<String, By> loadLocators(String jsonFolderPath) {
        Map<String, By> locators = new HashMap<>();

        for (File file : listJsonFiles(jsonFolderPath)) {
            JsonNode rootNode = readTree(file);

            if (rootNode.has("elements")) {
                rootNode.get("elements").forEach(element -> {
                    String name = element.get("elementName").asText();
                    String type = element.get("locatorType").asText();
                    String value = element.get("locatorValue").asText();

                    locators.put(name, convertToBy(type, value));
                });
            }
        }

        return locators;
    }

    private Map<String, String> loadTestData(String jsonFolderPath) {
        Map<String, String> testData = new HashMap<>();

        for (File file : listJsonFiles(jsonFolderPath)) {
            JsonNode rootNode = readTree(file);

            if (rootNode.has("testData")) {
                rootNode.get("testData").forEach(data -> {
                    String name = data.get("key").asText();
                    String value = data.get("value").asText();

                    testData.put(name, value);
                });
            }
        }

        return testData;
    }

    private File[] listJsonFiles(String jsonFolderPath) {
        File folder = new File(jsonFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (files == null) {
            throw new RuntimeException("Specified folder not found or empty: " + jsonFolderPath);
        }

        return files;
    }

    private JsonNode readTree(File file) {
        try {
            return MAPPER.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read JSON file: " + file.getName(),
                    e
            );
        }
    }

    private By convertToBy(String locatorType, String locatorValue) {
        return switch (locatorType.toLowerCase()) {
            case "xpath" -> By.xpath(locatorValue);
            case "id" -> By.id(locatorValue);
            case "css", "cssselector" -> By.cssSelector(locatorValue);
            case "name" -> By.name(locatorValue);
            case "class", "classname" -> By.className(locatorValue);
            default -> throw new IllegalArgumentException(
                    "Unsupported locator type: " + locatorType
            );
        };
    }

    public By getLocator(String elementName) {
        if (!locatorMap.containsKey(elementName)) {
            throw new RuntimeException("Locator not found: " + elementName);
        }

        return locatorMap.get(elementName);
    }

    public String getTestData(String elementName) {
        if (!testDataMap.containsKey(elementName)) {
            throw new RuntimeException("Test data not found: " + elementName);
        }

        return testDataMap.get(elementName);
    }
}
