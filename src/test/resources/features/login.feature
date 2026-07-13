@run
Feature: Login Page Tests

  Background:
    Given I navigate to "Sauce Demo"

  Scenario: LG-01 Successful login with valid standard user
    When I enter the data "Standard User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Products Title" should be visible

  Scenario: LG-02 Login with locked-out user
    When I enter the data "Locked Out User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Sorry, this user has been locked out"

  Scenario: LG-03 Login with invalid username
    When I enter the data "Invalid User" into the "Username Input"
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Username and password do not match any user in this service"

  Scenario: LG-04 Login with valid username and wrong password
    When I enter the data "Standard User" into the "Username Input"
    When I enter the data "Wrong Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Username and password do not match any user in this service"

  Scenario: LG-05 Login with empty username field
    When I enter the data "Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Username is required"

  Scenario: LG-06 Login with empty password field
    When I enter the data "Standard User" into the "Username Input"
    And I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Password is required"

  Scenario: LG-07 Login with both fields empty
    When I click the "Login Button"
    Then the "Error Message" should contain "Epic sadface: Username is required"

  Scenario: LG-08 Direct URL access to inventory without login
    When I navigate to "Sauce Demo - Inventory URL"
    Then the "Error Message" should contain "Epic sadface: You can only access '/inventory.html' when you are logged in"

  @excluded-from-ci
  Scenario: LG-09 Invalid credentials should not show products title (Fail Scenario)
    When I enter the data "Invalid User" into the "Username Input"
    When I enter the data "Wrong Password" into the "Password Input"
    And I click the "Login Button"
    Then the "Products Title" should be visible