package com.leadingeuropeanbank.testautomation.steps;

import com.leadingeuropeanbank.testautomation.hooks.Hooks;
import com.leadingeuropeanbank.testautomation.models.*;
import com.leadingeuropeanbank.testautomation.utils.DatabaseResultSet;
import com.leadingeuropeanbank.testautomation.utils.ExcelReportGenerator;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.util.*;

public class AggrLevel4Steps {

    private static final Logger logger = LogManager.getLogger(AggrLevel4Steps.class);

    private DatabaseResultSet dbResultSet = new DatabaseResultSet();
    private List<Map<String, Object>> aggrRecords;
    private List<Map<String, String>> testResults = new ArrayList<>();
    private Scenario testScenario;

    @Before
    public void beforeScenario(Scenario scenario) {
        this.testScenario = scenario;
        logger.info("======================================================");
        logger.info("STARTING SCENARIO: " + scenario.getName());
        logger.info("======================================================");
    }

    @When("I fetch the top {int} records for Level 4 aggregation")
    public void fetchTopRecords(int recordLimit) throws Exception {
        logger.info("========== STEP 1: Fetching Aggregation Records ==========");
        
        aggrRecords = dbResultSet.getTopAggrRecords(Hooks.prepordlhoneConn, recordLimit, "4");
        
        // HARD ASSERTIONS: Fail immediately if DB connection is bad or no data exists
        Assert.assertNotNull("CRITICAL FAILURE: The returned aggregation records list is null!", aggrRecords);
        Assert.assertFalse("CRITICAL FAILURE: No records found in lhone_aggr_pos for Level 4!", aggrRecords.isEmpty());
        
        logger.info("Successfully fetched {} records from lhone_aggr_pos.", aggrRecords.size());
    }

    @Then("I sequentially validate each record through the Level 4 pipeline for Monitor {int}")
    public void sequentiallyValidateEachRecord(int monitorId) throws Exception {
        logger.info("========== STEP 2: Processing Records Sequentially for Monitor ID: {} ==========", monitorId);

        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            String parentEntity = (String) record.get("PARENT_ENTITY");
            
            logger.info("------------------------------------------------------");
            logger.info("▶ STARTING PIPELINE FOR RECORD {} | LEI: {} | Parent: {}", (i + 1), lei, parentEntity);
            logger.debug("RAW AGGR RECORD VALUES: {}", record.toString());

            Map<String, String> reportRow = initializeReportRow(record);

            boolean isEligible = validateEligibilityForRecord(record, reportRow, monitorId);
            validateCategoryForRecord(record, reportRow, isEligible);
            boolean isSubCatValid = validateSubCategoryForRecord(record, reportRow, isEligible);
            boolean isQtyValid = calculateAndValidateQuantityForRecord(record, reportRow, isSubCatValid);
            calculateAndValidatePercentageForRecord(record, reportRow, isSubCatValid, isQtyValid);

            testResults.add(reportRow);
            logger.info("⏹ FINISHED PIPELINE FOR RECORD {} | Overall Status: {}", (i + 1), reportRow.get("Overall Status"));
        }
    }

    @Then("I generate the detailed Excel validation report")
    public void generateExcelReport() {
        logger.info("========== STEP 3: Generating Report & Final Assertions ==========");
        
        ExcelReportGenerator.generateReport(testResults, "Level4_Aggregation_Report");
        
        int failedCount = 0;
        for (Map<String, String> row : testResults) {
            if ("FAILED".equals(row.get("Overall Status"))) {
                failedCount++;
            }
        }

        // HARD ASSERTION: This is where we officially pass or fail the Cucumber Step
        if (failedCount > 0) {
            logger.error("❌ FINAL ASSERTION FAILED: {} out of {} records failed database validation.", failedCount, testResults.size());
            Assert.fail("Database Validation Failed for " + failedCount + " records. Please review the generated Excel report in the target folder.");
        } else {
            logger.info("✅ FINAL ASSERTION PASSED: All {} records successfully validated.", testResults.size());
            Assert.assertTrue("All records passed validation.", true);
        }
    }

    // ==================== PIPELINE HELPER METHODS ====================

    private Map<String, String> initializeReportRow(Map<String, Object> record) {
        Map<String, String> resultRow = new LinkedHashMap<>();
        resultRow.put("LEI", (String) record.get("SECURITY_IDENTIFIER_VALUE"));
        resultRow.put("Parent Entity", (String) record.get("PARENT_ENTITY"));
        resultRow.put("Aggr Report Category", (String) record.get("REPORT_CATEGORY"));
        resultRow.put("Aggr Sub-Category", (String) record.get("REPORT_SUB_CATEGORY"));
        resultRow.put("Expected Holdings Qty", String.valueOf(record.get("HOLDINGS_QUANTITY")));
        resultRow.put("Expected Holding %", String.valueOf(record.get("HOLDING_PERCENTAGE")));
        
        resultRow.put("Eligibility Status", "Pending");
        resultRow.put("Category Status", "Pending");
        resultRow.put("Sub-Category Status", "Pending");
        resultRow.put("Calculated Qty", "Pending");
        resultRow.put("Quantity Status", "Pending");
        resultRow.put("Calculated %", "Pending");
        resultRow.put("Percentage Status", "Pending");
        resultRow.put("Overall Status", "Pending");
        return resultRow;
    }

    private boolean validateEligibilityForRecord(Map<String, Object> record, Map<String, String> reportRow, int monitorId) throws Exception {
        String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
        String parentEntity = (String) record.get("PARENT_ENTITY");

        List<Map<String, Object>> stageRecords = dbResultSet.getStagingRecords(Hooks.prepordlhoneConn, lei, parentEntity);
        logger.info("  -> ELIGIBILITY: Found {} staging records for LEI: {}", stageRecords.size(), lei);

        if (stageRecords.isEmpty()) {
            logger.error("     [X] FAILED: No staging records found.");
            reportRow.put("Eligibility Status", "FAILED - Entity not found in stage table");
            return false;
        }

        Set<String> holdingClasses = new HashSet<>();
        boolean isEligible = false;

        for (Map<String, Object> stageRecord : stageRecords) {
            String fileId = String.valueOf(stageRecord.get("FILE_ID"));
            String landingRecId = String.valueOf(stageRecord.get("LANDING_REC_ID"));
            String holdingClass = (String) stageRecord.get("HOLDING_CLASSIFICATION");

            logger.debug("     Checking FILE_ID: {}, LANDING_REC_ID: {} against Monitor: {}", fileId, landingRecId, monitorId);

            if (dbResultSet.checkEligibility(Hooks.prepordlhoneConn, fileId, landingRecId, monitorId)) {
                isEligible = true;
                if (holdingClass != null) holdingClasses.add(holdingClass);
            }
        }

        if (isEligible) {
            logger.info("     [✓] PASSED: Eligibility confirmed. Holding Classes extracted: {}", holdingClasses);
            reportRow.put("Eligibility Status", "PASSED");
            record.put("HOLDING_CLASSES", holdingClasses);
            return true;
        } else {
            logger.error("     [X] FAILED: Found in staging, but no matching eligibility record found.");
            reportRow.put("Eligibility Status", "FAILED - Found in stage but NOT eligible");
            return false;
        }
    }

    private void validateCategoryForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean continueValidation) throws Exception {
        if (!continueValidation) {
            logger.warn("  -> CATEGORY: Skipped due to previous failure.");
            reportRow.put("Category Status", "SKIPPED");
            return;
        }

        String rptCategory = (String) record.get("REPORT_CATEGORY");
        logger.info("  -> CATEGORY: Validating Aggregation Category: '{}'", rptCategory);

        if ("Non-Exempt".equalsIgnoreCase(rptCategory)) {
            logger.info("     [✓] PASSED: Native Non-Exempt match.");
            reportRow.put("Category Status", "PASSED - Native Non-Exempt");
        } else {
            Set<String> holdingClasses = (Set<String>) record.get("HOLDING_CLASSES");
            boolean foundInMonitor = dbResultSet.validateCategoryFallback(Hooks.prepordlhoneConn, holdingClasses);
            
            if (foundInMonitor) {
                logger.info("     [✓] PASSED: Successfully mapped fallback categories from monitor.");
                reportRow.put("Category Status", "PASSED - Fallback mapped");
            } else {
                logger.info("     [✓] PASSED: No monitor mapping found. Defaulted to Non-Exempt.");
                reportRow.put("Category Status", "PASSED - Defaulted to Non-Exempt");
            }
        }
    }

    private boolean validateSubCategoryForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean continueValidation) throws Exception {
        if (!continueValidation) {
            logger.warn("  -> SUB-CATEGORY: Skipped due to previous failure.");
            reportRow.put("Sub-Category Status", "SKIPPED");
            return false;
        }

        String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
        String parentEntity = (String) record.get("PARENT_ENTITY");
        String subCategory = (String) record.get("REPORT_SUB_CATEGORY");

        logger.info("  -> SUB-CATEGORY: Validating against monitor for '{}'", subCategory);

        if (!dbResultSet.isSubCategoryValid(Hooks.prepordlhoneConn, subCategory)) {
            logger.error("     [X] FAILED: '{}' does not exist in lhone_monitor_rpt_sub_cat.", subCategory);
            reportRow.put("Sub-Category Status", "FAILED - Missing in monitor table");
            return false;
        }

        Map<String, Set<String>> mappings = dbResultSet.getProductMappings(Hooks.prepordlhoneConn, subCategory);
        logger.debug("     Extracted Mappings from product_map: {}", mappings);

        if (mappings.get("PRODUCT_TYPE").isEmpty()) {
            logger.error("     [X] FAILED: No active product mappings found for UK_LONG_SCOPE.");
            reportRow.put("Sub-Category Status", "FAILED - No product mappings");
            return false;
        }

        List<Map<String, Object>> validStageRecords = dbResultSet.getStagingRecordsByProductMap(Hooks.prepordlhoneConn, lei, parentEntity, mappings);
        logger.info("     Found {} staging records matching product mappings.", validStageRecords.size());

        if (validStageRecords.isEmpty()) {
            logger.error("     [X] FAILED: Staging records exist, but none match the required product mappings.");
            reportRow.put("Sub-Category Status", "FAILED - Stage mappings mismatch");
            return false;
        }

        logger.info("     [✓] PASSED: Sub-Category valid and mappings confirmed.");
        record.put("VALID_STAGE_RECORDS", validStageRecords);
        reportRow.put("Sub-Category Status", "PASSED");
        return true;
    }

    private boolean calculateAndValidateQuantityForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean continueValidation) {
        if (!continueValidation || record.get("VALID_STAGE_RECORDS") == null) {
            logger.warn("  -> QUANTITY: Skipped due to previous failure.");
            reportRow.put("Quantity Status", "SKIPPED");
            return false;
        }

        List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
        double totalUlQuantity = 0.0;

        logger.info("  -> QUANTITY: Calculating total across {} valid staging records.", stageRecords.size());

        for (Map<String, Object> stageRec : stageRecords) {
            String indicator = (String) stageRec.get("LONG_SHORT_INDICATOR");
            double delta = getDoubleValue(stageRec.get("DELTA_UL_QUANTITY"));

            logger.debug("     Processing Row - Indicator: {}, Delta: {}", indicator, delta);

            if ("S".equalsIgnoreCase(indicator) || "SHORT".equalsIgnoreCase(indicator)) {
                totalUlQuantity -= delta;
            } else {
                totalUlQuantity += delta;
            }
        }

        if (totalUlQuantity <= 0) {
            logger.debug("     Total calculated as <= 0. Defaulting final value to 0.0.");
            totalUlQuantity = 0.0;
        }

        record.put("CALCULATED_TOTAL_QTY", totalUlQuantity);
        reportRow.put("Calculated Qty", String.format("%.2f", totalUlQuantity));

        double expectedHoldings = getDoubleValue(record.get("HOLDINGS_QUANTITY"));
        
        logger.info("     Validation -> Expected: {}, Calculated: {}", expectedHoldings, totalUlQuantity);

        if (Math.abs(totalUlQuantity - expectedHoldings) < 0.0001) {
            logger.info("     [✓] PASSED: Quantities match.");
            reportRow.put("Quantity Status", "PASSED");
            return true;
        } else {
            logger.error("     [X] FAILED: Quantity mismatch detected.");
            reportRow.put("Quantity Status", "FAILED - Mismatch");
            return false;
        }
    }

    private void calculateAndValidatePercentageForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean subCatValid, boolean qtyValid) {
        if (!subCatValid || record.get("VALID_STAGE_RECORDS") == null) {
            logger.warn("  -> PERCENTAGE: Skipped due to previous failure.");
            reportRow.put("Percentage Status", "SKIPPED");
            reportRow.put("Overall Status", "FAILED");
            return;
        }

        List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
        double totalUlQuantity = (Double) record.get("CALCULATED_TOTAL_QTY");
        
        double votingRights = getDoubleValue(stageRecords.get(0).get("ISSUER_VOTING_RIGHTS_OUTSTANDING"));
        double expectedPercentage = getDoubleValue(record.get("HOLDING_PERCENTAGE"));

        logger.info("  -> PERCENTAGE: Calculating based on Voting Rights: {}", votingRights);

        if (votingRights <= 0) {
            logger.error("     [X] FAILED: Voting rights cannot be zero or negative.");
            reportRow.put("Percentage Status", "FAILED - Voting rights is 0 or null");
            reportRow.put("Calculated %", "N/A");
            reportRow.put("Overall Status", "FAILED");
            return;
        }

        double calculatedPercentage = (totalUlQuantity / votingRights) * 100;
        reportRow.put("Calculated %", String.format("%.4f", calculatedPercentage));

        logger.info("     Validation -> Expected: {}%, Calculated: {}%", expectedPercentage, calculatedPercentage);

        if (Math.abs(calculatedPercentage - expectedPercentage) < 0.0001) {
            logger.info("     [✓] PASSED: Holding Percentage matches.");
            reportRow.put("Percentage Status", "PASSED");
            
            if (qtyValid) {
                logger.info("  => OVERALL RECORD STATUS: PASSED");
                reportRow.put("Overall Status", "PASSED");
            } else {
                logger.warn("  => OVERALL RECORD STATUS: FAILED (Failed Quantity step earlier)");
                reportRow.put("Overall Status", "FAILED");
            }
        } else {
            logger.error("     [X] FAILED: Percentage mismatch detected.");
            reportRow.put("Percentage Status", "FAILED - Mismatch (Expected: " + expectedPercentage + ")");
            reportRow.put("Overall Status", "FAILED");
        }
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj != null) {
            try { return Double.parseDouble(obj.toString()); } 
            catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }
}
