package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.AlertActions;
import io.cucumber.java.en.When;

public class AlertSteps {
    private final AlertActions alertActions;

    public AlertSteps(AlertActions alertActions) {
        this.alertActions = alertActions;
    }

    @When("I accept the alert")
    public void acceptAlert(){
        alertActions.accept();
    }

    @When("I dismiss the alert")
    public void dismissAlert(){
        alertActions.dismiss();
    }
}
