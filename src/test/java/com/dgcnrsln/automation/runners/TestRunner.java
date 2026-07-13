package com.dgcnrsln.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(

        features = {"src/test/resources/features"},
        glue = {
                "com.dgcnrsln.automation.stepdefinitions",
                "com.dgcnrsln.automation.hooks"
        },
        tags = "@run and not @excluded-from-ci",
        plugin = {
                "summary",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "rerun:target/rerun.txt"
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
