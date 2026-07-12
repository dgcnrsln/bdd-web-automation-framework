package com.dgcnrsln.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(

        features = {"@target/rerun.txt"},
        glue = {
                "com.dgcnrsln.automation.stepdefinitions",
                "com.dgcnrsln.automation.hooks"
        },
        plugin = {
                "summary",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class RerunRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
