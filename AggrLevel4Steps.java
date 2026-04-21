package stepdefinitions;

import io.cucumber.java.en.*;
import utils.DatabaseResultSet;
import utils.ExcelReportGenerator;
import utils.Hooks;
import java.util.*;

public class AggrLevel4Steps {

    DatabaseResultSet dbResultSet = new DatabaseResultSet();
    List<Map<String, Object>> aggrRecords;
    List<Map<String, String>> testResults = new ArrayList<>();

    @When("I fetch the top {int} records for Level 4 aggregation")
    public void fetchTopRecords(int recordLimit) throws Exception {
        aggrRecords = dbResultSet.getTopAggrRecords(Hooks.prepordlhoneConn, recordLimit, "4");
        System.out.println("Fetched " + aggrRecords.size() + " records from lhone_aggr_pos.");
        
        for (Map<String, Object> record : aggrRecords) {
            Map<String, String> resultRow = new LinkedHashMap<>();
            resultRow.put("LEI", (String) record.get("SECURITY_IDENTIFIER_VALUE"));
            resultRow.put("Parent Entity", (String) record.get("PARENT_ENTITY"));
            resultRow.put("Aggr Report Category", (String) record.get("REPORT_CATEGORY"));
            resultRow.put("Aggr Sub-Category", (String) record.get("REPORT_SUB_CATEGORY"));
            
            // Expected Values
            resultRow.put("Expected Holdings Qty", String.valueOf(record.get("HOLDINGS_QUANTITY")));
            resultRow.put("Expected Holding %", String.valueOf(record.get("HOLDING_PERCENTAGE")));
            
            // Status Tracking
            resultRow.put("Eligibility Status", "Pending");
            resultRow.put("Category Status", "Pending");
            resultRow.put("Sub-Category Status", "Pending");
            resultRow.put("Calculated Qty", "Pending");
            resultRow.put("Quantity Status", "Pending");
            resultRow.put("Calculated %", "Pending");
            resultRow.put("Percentage Status", "Pending");
            resultRow.put("Overall Status", "Pending");
            
            testResults.add(resultRow);
        }
    }

    @Then("I validate the base entity data and staging eligibility for Monitor {int}")
    public void validateEligibility(int monitorId) throws Exception {
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            String parentEntity = (String) record.get("PARENT_ENTITY");

            List<Map<String, Object>> stageRecords = dbResultSet.getStagingRecords(Hooks.prepordlhoneConn, lei, parentEntity);
            if (stageRecords.isEmpty()) {
                reportRow.put("Eligibility Status", "FAILED - Entity not found in stage table");
                record.put("IS_VALID", false);
                continue;
            }

            Set<String> holdingClasses = new HashSet<>();
            boolean isEligible = false;

            for (Map<String, Object> stageRecord : stageRecords) {
                String fileId = String.valueOf(stageRecord.get("FILE_ID"));
                String landingRecId = String.valueOf(stageRecord.get("LANDING_REC_ID"));
                String holdingClass = (String) stageRecord.get("HOLDING_CLASSIFICATION");

                if (dbResultSet.checkEligibility(Hooks.prepordlhoneConn, fileId, landingRecId, monitorId)) {
                    isEligible = true;
                    if (holdingClass != null) holdingClasses.add(holdingClass);
                }
            }

            if (isEligible) {
                reportRow.put("Eligibility Status", "PASSED");
                record.put("HOLDING_CLASSES", holdingClasses);
                record.put("IS_VALID", true);
            } else {
                reportRow.put("Eligibility Status", "FAILED - Found in stage but NOT eligible");
                record.put("IS_VALID", false);
            }
        }
    }

    @Then("I validate the Report Category classification logic")
    public void validateReportCategory() throws Exception {
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);

            if (record.get("IS_VALID") != null && !(Boolean) record.get("IS_VALID")) {
                reportRow.put("Category Status", "SKIPPED");
                continue;
            }

            String rptCategory = (String) record.get("REPORT_CATEGORY");

            if ("Non-Exempt".equalsIgnoreCase(rptCategory)) {
                reportRow.put("Category Status", "PASSED - Native Non-Exempt");
            } else {
                Set<String> holdingClasses = (Set<String>) record.get("HOLDING_CLASSES");
                boolean foundInMonitor = dbResultSet.validateCategoryFallback(Hooks.prepordlhoneConn, holdingClasses);
                
                if (foundInMonitor) {
                    reportRow.put("Category Status", "PASSED - Fallback mapped");
                } else {
                    reportRow.put("Category Status", "PASSED - Defaulted to Non-Exempt");
                }
            }
        }
    }

    @Then("I validate the Report Sub-Category against product mappings")
    public void validateSubCategoryMappings() throws Exception {
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);

            if (record.get("IS_VALID") != null && !(Boolean) record.get("IS_VALID")) {
                reportRow.put("Sub-Category Status", "SKIPPED");
                continue;
            }

            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            String parentEntity = (String) record.get("PARENT_ENTITY");
            String subCategory = (String) record.get("REPORT_SUB_CATEGORY");

            if (!dbResultSet.isSubCategoryValid(Hooks.prepordlhoneConn, subCategory)) {
                reportRow.put("Sub-Category Status", "FAILED - Missing in monitor table");
                record.put("IS_VALID", false);
                continue;
            }

            Map<String, Set<String>> mappings = dbResultSet.getProductMappings(Hooks.prepordlhoneConn, subCategory);
            if (mappings.get("PRODUCT_TYPE").isEmpty()) {
                reportRow.put("Sub-Category Status", "FAILED - No product mappings");
                record.put("IS_VALID", false);
                continue;
            }

            List<Map<String, Object>> validStageRecords = dbResultSet.getStagingRecordsByProductMap(Hooks.prepordlhoneConn, lei, parentEntity, mappings);
            if (validStageRecords.isEmpty()) {
                reportRow.put("Sub-Category Status", "FAILED - Stage mappings mismatch");
                record.put("IS_VALID", false);
                continue;
            }

            record.put("VALID_STAGE_RECORDS", validStageRecords);
            reportRow.put("Sub-Category Status", "PASSED");
        }
    }

    @Then("I calculate the total underlying quantity and validate against holdings")
    public void calculateAndValidateQuantity() throws Exception {
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);

            if (record.get("IS_VALID") != null && !(Boolean) record.get("IS_VALID")) {
                reportRow.put("Quantity Status", "SKIPPED");
                continue;
            }

            List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
            double totalUlQuantity = 0.0;

            for (Map<String, Object> stageRec : stageRecords) {
                String indicator = (String) stageRec.get("LONG_SHORT_INDICATOR");
                double delta = getDoubleValue(stageRec.get("DELTA_UL_QUANTITY"));

                if ("S".equalsIgnoreCase(indicator) || "SHORT".equalsIgnoreCase(indicator)) {
                    totalUlQuantity -= delta;
                } else {
                    totalUlQuantity += delta;
                }
            }

            if (totalUlQuantity <= 0) totalUlQuantity = 0.0;

            // Store calculated quantity for the next step (Holding Percentage)
            record.put("CALCULATED_TOTAL_QTY", totalUlQuantity);
            reportRow.put("Calculated Qty", String.format("%.2f", totalUlQuantity));

            double expectedHoldings = getDoubleValue(record.get("HOLDINGS_QUANTITY"));
            
            if (Math.abs(totalUlQuantity - expectedHoldings) < 0.0001) {
                reportRow.put("Quantity Status", "PASSED");
            } else {
                reportRow.put("Quantity Status", "FAILED - Mismatch");
                record.put("IS_VALID", false); // Fail overall but keep going
            }
        }
    }

    @Then("I calculate the holding percentage and validate against aggregation holdings")
    public void calculateAndValidateHoldingPercentage() throws Exception {
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);

            // Skip if critical previous steps failed or valid stage records are missing
            if (record.get("VALID_STAGE_RECORDS") == null) {
                reportRow.put("Percentage Status", "SKIPPED");
                reportRow.put("Overall Status", "FAILED");
                continue;
            }

            List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
            double totalUlQuantity = (Double) record.get("CALCULATED_TOTAL_QTY");
            
            // Extract ISSUER_VOTING_RIGHTS_OUTSTANDING from the first record (as it's the same for all)
            double votingRights = getDoubleValue(stageRecords.get(0).get("ISSUER_VOTING_RIGHTS_OUTSTANDING"));
            double expectedPercentage = getDoubleValue(record.get("HOLDING_PERCENTAGE"));

            if (votingRights <= 0) {
                reportRow.put("Percentage Status", "FAILED - Voting rights is 0 or null");
                reportRow.put("Calculated %", "N/A");
                reportRow.put("Overall Status", "FAILED");
                continue;
            }

            // Calculation
            double calculatedPercentage = (totalUlQuantity / votingRights) * 100;
            reportRow.put("Calculated %", String.format("%.4f", calculatedPercentage));

            // Validate with a small tolerance for floating point precision
            if (Math.abs(calculatedPercentage - expectedPercentage) < 0.0001) {
                reportRow.put("Percentage Status", "PASSED");
                
                // If previous Quantity status was also PASSED, set Overall to PASSED
                if ("PASSED".equals(reportRow.get("Quantity Status"))) {
                    reportRow.put("Overall Status", "PASSED");
                } else {
                    reportRow.put("Overall Status", "FAILED");
                }
            } else {
                reportRow.put("Percentage Status", "FAILED - Mismatch (Expected: " + expectedPercentage + ")");
                reportRow.put("Overall Status", "FAILED");
            }
        }
    }

    @Then("I generate the detailed Excel validation report")
    public void generateExcelReport() {
        ExcelReportGenerator.generateReport(testResults, "Level4_Aggregation_Report");
    }

    // Helper method to safely parse Object to double from DB ResultSet
    private double getDoubleValue(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj != null) {
            try { return Double.parseDouble(obj.toString()); } 
            catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }
}
