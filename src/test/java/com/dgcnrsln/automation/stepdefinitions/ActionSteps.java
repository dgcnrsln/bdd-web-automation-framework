package com.dgcnrsln.automation.stepdefinitions;

import com.dgcnrsln.automation.actions.ElementActions;
import com.dgcnrsln.automation.repositories.DataRepository;
import com.dgcnrsln.automation.repositories.LocatorRepository;
import io.cucumber.java.en.When;

public class ActionSteps {
    private final LocatorRepository locatorRepository;
    private final DataRepository dataRepository;
    private final ElementActions elementActions;

    public ActionSteps(LocatorRepository locatorRepository,
                       DataRepository dataRepository,
                       ElementActions elementActions) {
        this.locatorRepository = locatorRepository;
        this.dataRepository = dataRepository;
        this.elementActions = elementActions;
    }

    @When("I click the {string}")
    public void clickElement(String elementName) {
        elementActions.click(
                locatorRepository.getLocator(elementName)
        );
    }

    @When("I click the {string} with JavaScript")
    public void javascriptClick(String elementName) {
        elementActions.javascriptClick(
                locatorRepository.getLocator(elementName)
        );
    }

    @When("I enter {string} into the {string}")
    public void enterText(String text, String elementName) {
        elementActions.type(
                text,
                locatorRepository.getLocator(elementName)
        );
    }


    @When("I enter the data {string} into the {string}")
    public void enterTextFromData(String text, String elementName) {
        elementActions.type(
                dataRepository.getTestData(text),
                locatorRepository.getLocator(elementName)
        );
    }


    @When("I clear the {string}")
    public void clearElement(String elementName) {
        elementActions.clear(
                locatorRepository.getLocator(elementName)
        );
    }

    @When("I scroll to the {string}")
    public void scrollElement(String elementName) {
        elementActions.scroll(
                locatorRepository.getLocator(elementName)
        );
    }
}
