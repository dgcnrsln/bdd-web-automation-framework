@run
Feature: Inventory Page Tests

  Background:
    Given I navigate to "Sauce Demo"
    When I enter the data "Standard User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"

  Scenario: INV-01 Cart badge shows count after adding product
    When I click the "Add To Cart"
    Then the "Cart Badge" should contain "1"

  Scenario: INV-02 Cart badge disappears after removing product
    When I click the "Add To Cart"
    When I click the "Inventory Remove Button"
    Then the "Add To Cart" should be visible

  Scenario: INV-03 Burger menu shows logout link
    When I click the "Menu Button"
    Then the "Logout Link" should be visible

  Scenario: INV-04 Logout via menu returns to login page
    When I click the "Menu Button"
    When I click the "Logout Link"
    Then the "Login Button" should be visible

  Scenario: INV-05 Clicking product name opens product detail page
    When I click the "Sauce Labs Backpack"
    Then the "Product Detail Name" should contain "Sauce Labs Backpack"

  Scenario: INV-06 Product image is visible on inventory page
    Then the "Inventory Item Image" should be visible
