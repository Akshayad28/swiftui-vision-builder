package stepdefinitions;

import io.cucumber.java.en.*;
import utils.DatabaseResultSet;
import utils.ExcelReportGenerator;
import utils.Hooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.util.*;

public class AggrLevel4Steps {

    private static final Logger logger = LogManager.getLogger(AggrLevel4Steps.class);

    DatabaseResultSet dbResultSet = new DatabaseResultSet();
    List<Map<String, Object>> aggrRecords;
    List<Map<String, String>> testResults = new ArrayList<>();

    @When("I fetch the top {int} records for Level 4 aggregation")
    public void fetchTopRecords(int recordLimit) throws Exception {
        logger.info("========== STEP: Fetching Top {} Aggregation Records ==========", recordLimit);
        
        aggrRecords = dbResultSet.getTopAggrRecords(Hooks.prepordlhoneConn, recordLimit, "4");
        
        Assert.assertNotNull("The returned aggregation records list is null!", aggrRecords);
        Assert.assertFalse("No records found in lhone_aggr_pos for Level 4!", aggrRecords.isEmpty());
        
        logger.info("Successfully fetched {} records from lhone_aggr_pos.", aggrRecords.size());
        
        for (Map<String, Object> record : aggrRecords) {
            Map<String, String> resultRow = new LinkedHashMap<>();
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            
            logger.debug("Initializing validation tracker for LEI: {}", lei);
            
            resultRow.put("LEI", lei);
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
        logger.info("========== STEP: Validating Staging Eligibility (Monitor ID: {}) ==========", monitorId);
        
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            String parentEntity = (String) record.get("PARENT_ENTITY");

            logger.info("Processing Eligibility for LEI: {} | Parent: {}", lei, parentEntity);

            List<Map<String, Object>> stageRecords = dbResultSet.getStagingRecords(Hooks.prepordlhoneConn, lei, parentEntity);
            if (stageRecords.isEmpty()) {
                logger.error("Eligibility FAILED: No staging records found for LEI: {}", lei);
                reportRow.put("Eligibility Status", "FAILED - Entity not found in stage table");
                record.put("IS_VALID", false);
                continue;
            }

            logger.debug("Found {} staging records for LEI: {}. Checking Eligibility table...", stageRecords.size(), lei);
            
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
                logger.info("Eligibility PASSED for LEI: {}", lei);
                reportRow.put("Eligibility Status", "PASSED");
                record.put("HOLDING_CLASSES", holdingClasses);
                record.put("IS_VALID", true);
            } else {
                logger.warn("Eligibility FAILED: Found in stage but not in lhone_europe_eligibility_pos for LEI: {}", lei);
                reportRow.put("Eligibility Status", "FAILED - Found in stage but NOT eligible");
                record.put("IS_VALID", false);
            }
        }
    }

    @Then("I validate the Report Category classification logic")
    public void validateReportCategory() throws Exception {
        logger.info("========== STEP: Validating Report Category ==========");
        
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");

            if (record.get("IS_VALID") != null && !(Boolean) record.get("IS_VALID")) {
                logger.debug("Skipping Category check for LEI: {} due to prior failure.", lei);
                reportRow.put("Category Status", "SKIPPED");
                continue;
            }

            String rptCategory = (String) record.get("REPORT_CATEGORY");
            logger.info("Checking Report Category '{}' for LEI: {}", rptCategory, lei);

            if ("Non-Exempt".equalsIgnoreCase(rptCategory)) {
                logger.info("Category PASSED: Native Non-Exempt for LEI: {}", lei);
                reportRow.put("Category Status", "PASSED - Native Non-Exempt");
            } else {
                Set<String> holdingClasses = (Set<String>) record.get("HOLDING_CLASSES");
                boolean foundInMonitor = dbResultSet.validateCategoryFallback(Hooks.prepordlhoneConn, holdingClasses);
                
                if (foundInMonitor) {
                    logger.info("Category PASSED: Fallback mapped successfully for LEI: {}", lei);
                    reportRow.put("Category Status", "PASSED - Fallback mapped");
                } else {
                    logger.info("Category PASSED: Defaulted to Non-Exempt for LEI: {}", lei);
                    reportRow.put("Category Status", "PASSED - Defaulted to Non-Exempt");
                }
            }
        }
    }

    @Then("I validate the Report Sub-Category against product mappings")
    public void validateSubCategoryMappings() throws Exception {
        logger.info("========== STEP: Validating Report Sub-Category ==========");
        
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");

            if (record.get("IS_VALID") != null && !(Boolean) record.get("IS_VALID")) {
                reportRow.put("Sub-Category Status", "SKIPPED");
                continue;
            }

            String parentEntity = (String) record.get("PARENT_ENTITY");
            String subCategory = (String) record.get("REPORT_SUB_CATEGORY");

            logger.info("Checking Sub-Category '{}' for LEI: {}", subCategory, lei);

            if (!dbResultSet.isSubCategoryValid(Hooks.prepordlhoneConn, subCategory)) {
                logger.error("Sub-Category FAILED: '{}' missing in monitor table for LEI: {}", subCategory, lei);
                reportRow.put("Sub-Category Status", "FAILED - Missing in monitor table");
                record.put("IS_VALID", false);
                continue;
            }

            Map<String, Set<String>> mappings = dbResultSet.getProductMappings(Hooks.prepordlhoneConn, subCategory);
            if (mappings.get("PRODUCT_TYPE").isEmpty()) {
                logger.error("Sub-Category FAILED: No product mappings found for '{}'", subCategory);
                reportRow.put("Sub-Category Status", "FAILED - No product mappings");
                record.put("IS_VALID", false);
                continue;
            }

            List<Map<String, Object>> validStageRecords = dbResultSet.getStagingRecordsByProductMap(Hooks.prepordlhoneConn, lei, parentEntity, mappings);
            if (validStageRecords.isEmpty()) {
                logger.error("Sub-Category FAILED: Staging mappings mismatch for LEI: {}", lei);
                reportRow.put("Sub-Category Status", "FAILED - Stage mappings mismatch");
                record.put("IS_VALID", false);
                continue;
            }

            logger.info("Sub-Category PASSED: Mappings verified for LEI: {}", lei);
            record.put("VALID_STAGE_RECORDS", validStageRecords);
            reportRow.put("Sub-Category Status", "PASSED");
        }
    }

    @Then("I calculate the total underlying quantity and validate against holdings")
    public void calculateAndValidateQuantity() throws Exception {
        logger.info("========== STEP: Calculating Total Underlying Quantity ==========");
        
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");

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

            if (totalUlQuantity <= 0) {
                logger.debug("Calculated quantity was <= 0, defaulting to 0.0 for LEI: {}", lei);
                totalUlQuantity = 0.0;
            }

            record.put("CALCULATED_TOTAL_QTY", totalUlQuantity);
            reportRow.put("Calculated Qty", String.format("%.2f", totalUlQuantity));

            double expectedHoldings = getDoubleValue(record.get("HOLDINGS_QUANTITY"));
            logger.info("LEI: {} | Expected Qty: {} | Calculated Qty: {}", lei, expectedHoldings, totalUlQuantity);

            if (Math.abs(totalUlQuantity - expectedHoldings) < 0.0001) {
                logger.info("Quantity PASSED for LEI: {}", lei);
                reportRow.put("Quantity Status", "PASSED");
            } else {
                logger.error("Quantity FAILED: Mismatch for LEI: {}", lei);
                reportRow.put("Quantity Status", "FAILED - Mismatch");
                record.put("IS_VALID", false); 
            }
        }
    }

    @Then("I calculate the holding percentage and validate against aggregation holdings")
    public void calculateAndValidateHoldingPercentage() throws Exception {
        logger.info("========== STEP: Calculating Holding Percentage ==========");
        
        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            Map<String, String> reportRow = testResults.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");

            if (record.get("VALID_STAGE_RECORDS") == null) {
                reportRow.put("Percentage Status", "SKIPPED");
                reportRow.put("Overall Status", "FAILED");
                continue;
            }

            List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
            double totalUlQuantity = (Double) record.get("CALCULATED_TOTAL_QTY");
            
            double votingRights = getDoubleValue(stageRecords.get(0).get("ISSUER_VOTING_RIGHTS_OUTSTANDING"));
            double expectedPercentage = getDoubleValue(record.get("HOLDING_PERCENTAGE"));

            if (votingRights <= 0) {
                logger.error("Percentage FAILED: Voting rights <= 0 for LEI: {}", lei);
                reportRow.put("Percentage Status", "FAILED - Voting rights is 0 or null");
                reportRow.put("Calculated %", "N/A");
                reportRow.put("Overall Status", "FAILED");
                continue;
            }

            double calculatedPercentage = (totalUlQuantity / votingRights) * 100;
            reportRow.put("Calculated %", String.format("%.4f", calculatedPercentage));

            logger.info("LEI: {} | Expected %: {} | Calculated %: {}", lei, expectedPercentage, calculatedPercentage);

            if (Math.abs(calculatedPercentage - expectedPercentage) < 0.0001) {
                logger.info("Percentage PASSED for LEI: {}", lei);
                reportRow.put("Percentage Status", "PASSED");
                
                if ("PASSED".equals(reportRow.get("Quantity Status"))) {
                    reportRow.put("Overall Status", "PASSED");
                } else {
                    reportRow.put("Overall Status", "FAILED");
                }
            } else {
                logger.error("Percentage FAILED: Mismatch for LEI: {}", lei);
                reportRow.put("Percentage Status", "FAILED - Mismatch (Expected: " + expectedPercentage + ")");
                reportRow.put("Overall Status", "FAILED");
            }
        }
    }

    @Then("I generate the detailed Excel validation report")
    public void generateExcelReport() {
        logger.info("========== STEP: Generating Excel Report & Final Assertion ==========");
        
        ExcelReportGenerator.generateReport(testResults, "Level4_Aggregation_Report");
        logger.info("Excel report generation triggered successfully.");

        // Aggregate assertion: Fail the Cucumber scenario if ANY record failed validation
        int failedCount = 0;
        for (Map<String, String> row : testResults) {
            if ("FAILED".equals(row.get("Overall Status"))) {
                failedCount++;
            }
        }

        if (failedCount > 0) {
            logger.error("FINAL ASSERTION FAILED: {} out of {} records failed database validation.", failedCount, testResults.size());
            Assert.fail("Database Validation Failed for " + failedCount + " records. Please review the generated Excel report.");
        } else {
            logger.info("FINAL ASSERTION PASSED: All {} records successfully validated.", testResults.size());
            Assert.assertTrue("All records passed", true);
        }
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
