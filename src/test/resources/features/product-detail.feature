@run
Feature: Product Detail Page Tests

  Background:
    Given I navigate to "Sauce Demo"
    When I enter the data "Standard User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    And I click the "Sauce Labs Backpack"

  Scenario: PD-01 Product detail page displays correct name and price
    Then the "Product Detail Name" should contain "Sauce Labs Backpack"
    Then the "Product Detail Price" should contain the data "Backpack Price"

  Scenario: PD-02 Product detail page displays description and image
    Then the "Product Detail Description" should be visible
    Then the "Product Detail Image" should be visible

  Scenario: PD-03 Add product to cart from detail page
    When I click the "Product Detail Add To Cart"
    Then the "Cart Badge" should contain "1"

  Scenario: PD-04 Remove product from cart from detail page
    When I click the "Product Detail Add To Cart"
    When I click the "Product Detail Remove From Cart"
    Then the "Product Detail Add To Cart" should be visible

  Scenario: PD-05 Back to products button returns to inventory page
    When I click the "Back To Products"
    Then the "Products Title" should be visible