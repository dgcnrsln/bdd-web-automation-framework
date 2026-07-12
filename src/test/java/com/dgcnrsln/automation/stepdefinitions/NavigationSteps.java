package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.NavigationActions;
import com.dgcnrsln.automation.repositories.EnvironmentRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class NavigationSteps {
    private final NavigationActions navigationActions;
    private final EnvironmentRepository environmentRepository;

    public NavigationSteps(NavigationActions navigationActions,
                           EnvironmentRepository environmentRepository) {
        this.navigationActions = navigationActions;
        this.environmentRepository = environmentRepository;
    }


    @Given("I navigate to {string}")
    public void navigateTo(String environment) {
        String url = environmentRepository.getEnvironment(environment);
        navigationActions.goTo(url);
    }


    @When("I refresh the page")
    public void refreshPage() {
        navigationActions.refresh();
    }


    @When("I navigate back")
    public void navigateBack() {
        navigationActions.back();
    }


    @When("I navigate forward")
    public void navigateForward() {
        navigationActions.forward();
    }
}
