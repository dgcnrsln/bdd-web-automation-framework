package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.BrowserActions;
import io.cucumber.java.en.When;

public class BrowserSteps {
    private final BrowserActions browserActions;

    public BrowserSteps(BrowserActions browserActions) {
        this.browserActions = browserActions;
    }

    @When("I take a screenshot")
    public void takeScreenshot() {
        browserActions.takeScreenshot();
    }

    @When("I press the Enter key")
    public void pressTheEnterKey() {
        browserActions.pressEnter();
    }
}
