package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.WaitActions;
import com.dgcnrsln.automation.repositories.LocatorRepository;
import io.cucumber.java.en.When;

public class WaitSteps {
    private final LocatorRepository locatorRepository;
    private final WaitActions waitActions;

    public WaitSteps(LocatorRepository locatorRepository,
                                WaitActions waitActions) {
        this.locatorRepository = locatorRepository;
        this.waitActions = waitActions;
    }

    @When("I wait for the {string} to be visible")
    public void waitVisible(String elementName) {
        waitActions.visible(
                locatorRepository.getLocator(elementName)
        );
    }
}
