public class ExceptionValidationSteps {

    DBUtils dbUtils = new DBUtils();
    RuleEngine ruleEngine = new RuleEngine();

    List<Map<String, Object>> exceptionRecords;
    List<Boolean> results = new ArrayList<>();

    @Given("I fetch exception records from database")
    public void fetchExceptionRecords() {

        String query = "SELECT file_id, landing_rec_id, monitor_id, CASUAL_ATTRIBUTES FROM exception_table";

        exceptionRecords = dbUtils.executeQuery(query);

        System.out.println("Total Records: " + exceptionRecords.size());
    }

    @When("I validate each record using dynamic rule engine")
    public void validateRecords() {

        for (Map<String, Object> record : exceptionRecords) {

            String fileId = record.get("file_id").toString();
            String landingId = record.get("landing_rec_id").toString();
            int monitorId = Integer.parseInt(record.get("monitor_id").toString());
            String attribute = record.get("CASUAL_ATTRIBUTES").toString();

            String monitor = (monitorId == 2) ? "HK_LONG" : "UK_LONG";

            // 🔹 Load rules
            List<Rule> rules = ruleEngine.loadRules(monitor);

            // 🔹 Find rule
            Rule rule = ruleEngine.findMatchingRule(attribute, rules);

            // 🔹 Fetch DB record
            Map<String, Object> dbRecord =
                    dbUtils.getLhoneRecord(fileId, landingId);

            // 🔹 Column NULL validation
            Object columnValue = dbRecord.get(attribute);

            // 🔹 Rule validation
            boolean ruleResult =
                    ruleEngine.evaluateException(rule, dbRecord);

            boolean finalStatus = (columnValue == null && ruleResult);

            log(record, rule, columnValue, finalStatus);

            results.add(finalStatus);
        }
    }

    @Then("all exception records should be valid")
    public void validateFinal() {

        for (Boolean r : results) {
            Assert.assertTrue("Validation failed", r);
        }
    }

    private void log(Map<String, Object> record, Rule rule,
                     Object columnValue, boolean status) {

        System.out.println("=================================");
        System.out.println("File: " + record.get("file_id"));
        System.out.println("Landing: " + record.get("landing_rec_id"));
        System.out.println("Attribute: " + record.get("CASUAL_ATTRIBUTES"));
        System.out.println("Rule: " + rule.getRuleId());
        System.out.println("Column Value: " + columnValue);
        System.out.println("Final Status: " + status);
    }
}
