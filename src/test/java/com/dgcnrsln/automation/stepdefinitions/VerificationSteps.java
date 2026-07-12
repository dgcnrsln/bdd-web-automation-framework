package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.VerificationActions;
import com.dgcnrsln.automation.repositories.DataRepository;
import com.dgcnrsln.automation.repositories.LocatorRepository;
import io.cucumber.java.en.Then;

public class VerificationSteps {
    private final LocatorRepository locatorRepository;
    private final DataRepository dataRepository;
    private final VerificationActions verificationActions;

    public VerificationSteps(LocatorRepository locatorRepository,
                             DataRepository dataRepository,
                             VerificationActions verificationActions) {
        this.locatorRepository = locatorRepository;
        this.dataRepository = dataRepository;
        this.verificationActions = verificationActions;
    }

    @Then("the {string} should be visible")
    public void shouldBeVisible(String elementName) {
        verificationActions.verifyElementVisible(
                locatorRepository.getLocator(elementName)
        );
    }

    @Then("the {string} should not be visible")
    public void shouldNotBeVisible(String elementName) {
        verificationActions.verifyElementInvisible(
                locatorRepository.getLocator(elementName)
        );
    }

    @Then("the {string} should contain {string}")
    public void shouldContain(String elementName, String expectedText) {
        verificationActions.verifyTextContains(
                locatorRepository.getLocator(elementName),
                expectedText
        );
    }

    @Then("the {string} should contain the data {string}")
    public void shouldContainData(String elementName, String expectedText) {
        verificationActions.verifyTextContains(
                locatorRepository.getLocator(elementName),
                dataRepository.getTestData(expectedText)
        );
    }

    @Then("the {string} should have value {string}")
    public void shouldHaveValue(String elementName, String expectedValue) {
        verificationActions.verifyValueContains(
                locatorRepository.getLocator(elementName),
                expectedValue
        );
    }

    @Then("the {string} should have value from the data {string}")
    public void shouldHaveValueData(String elementName, String expectedValue) {
        verificationActions.verifyValueContains(
                locatorRepository.getLocator(elementName),
                dataRepository.getTestData(expectedValue)
        );
    }
}
