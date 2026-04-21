Feature: Level 4 Aggregation Database Validation

  @db_validation @level4
  Scenario Outline: Execute End-to-End Validations for Level 4 Aggregation
    Given the database connection to the preprod environment is established
    When I fetch the top <RecordLimit> records for Level 4 aggregation
    Then I validate the base entity data and staging eligibility for Monitor <MonitorID>
    And I validate the Report Category classification logic
    And I validate the Report Sub-Category against product mappings
    And I calculate the total underlying quantity and validate against holdings
    And I calculate the holding percentage and validate against aggregation holdings
    And I generate the detailed Excel validation report

    Examples:
      | RecordLimit | MonitorID |
      | 10          | 1         |
