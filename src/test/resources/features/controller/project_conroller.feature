Feature: Is project controller functions work properly?

  Scenario: Get projects page
    Given 10 created projects
    When get projects page with number 0, size 5
    Then received page with number 0, size 5
    And status code is 200

  Scenario: Get existing project by id
    Given 1 created projects
    When get project by id
    Then received project by id
    And status code is 200

  Scenario: Create new project
    Given 0 created projects
    When create new project
    Then received created project
    And status code is 201

  Scenario: Update created project
    Given 1 created projects
    When update created project
    Then received updated project
    And status code is 200

  Scenario: Delete existing project
    Given 1 created projects
    When delete project
    Then received response without content
    And project deleted
    And status code is 204