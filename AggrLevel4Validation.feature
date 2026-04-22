Feature: Level 4 Aggregation Database Validation

  @db_validation @level4
  Scenario Outline: Execute End-to-End Validations for Level 4 Aggregation
    Given the database connection to the preprod environment is established
    When I fetch the top <RecordLimit> records for Level 4 aggregation
    Then I sequentially validate each record through the Level 4 pipeline for Monitor <MonitorID>
    And I generate the detailed Excel validation report

    Examples:
      | RecordLimit | MonitorID |
      | 10          | 1         |
