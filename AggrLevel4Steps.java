@Then("I sequentially validate each record through the Level 4 pipeline for Monitor {int} and Job ID {int}")
    public void sequentiallyValidateEachRecord(int monitorId, int jobId) throws Exception {
        logger.info("========== STEP 2: Processing Records Sequentially ==========");
        logger.info("Parameters -> Monitor ID: {}, Job ID: {}", monitorId, jobId);
        
        this.jobId = jobId;

        for (int i = 0; i < aggrRecords.size(); i++) {
            Map<String, Object> record = aggrRecords.get(i);
            String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
            String parentEntity = (String) record.get("PARENT_ENTITY");
            
            logger.info("------------------------------------------------------");
            logger.info("▶ STARTING PIPELINE FOR RECORD {} | LEI: {} | Parent: {}", (i + 1), lei, parentEntity);

            Map<String, String> reportRow = initializeReportRow(record);

            boolean isEligible = validateEligibilityForRecord(record, reportRow, monitorId);
            validateCategoryForRecord(record, reportRow, isEligible);
            
            // UPDATED: Passing monitorId here so we can join eligibility in the DB call!
            boolean isSubCatValid = validateSubCategoryForRecord(record, reportRow, isEligible, monitorId);
            
            boolean isQtyValid = calculateAndValidateQuantityForRecord(record, reportRow, isSubCatValid);
            calculateAndValidatePercentageForRecord(record, reportRow, isSubCatValid, isQtyValid);

            testResults.add(reportRow);
            logger.info("⏹ FINISHED PIPELINE FOR RECORD {} | Overall Status: {}", (i + 1), reportRow.get("Overall Status"));
        }
    }

    // UPDATED: Added int monitorId to the parameters
    private boolean validateSubCategoryForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean continueValidation, int monitorId) throws Exception {
        if (!continueValidation) {
            reportRow.put("Sub-Category Status", "SKIPPED");
            return false;
        }

        String lei = (String) record.get("SECURITY_IDENTIFIER_VALUE");
        String parentEntity = (String) record.get("PARENT_ENTITY");
        String subCategory = (String) record.get("REPORT_SUB_CATEGORY");

        if (!dbResultSet.isSubCategoryValid(Hooks.prepordlhoneConn, subCategory)) {
            logger.error("     [X] FAILED: '{}' missing in monitor table.", subCategory);
            reportRow.put("Sub-Category Status", "FAILED - Missing in monitor table");
            return false;
        }

        Map<String, Set<String>> mappings = dbResultSet.getProductMappings(Hooks.prepordlhoneConn, subCategory);

        if (mappings.get("PRODUCT_TYPE").isEmpty()) {
            logger.error("     [X] FAILED: No active product mappings found.");
            reportRow.put("Sub-Category Status", "FAILED - No product mappings");
            return false;
        }

        // UPDATED: Passing monitorId down to the DB call
        List<Map<String, Object>> validStageRecords = dbResultSet.getStagingRecordsByProductMap(
                Hooks.prepordlhoneConn, lei, parentEntity, mappings, this.jobId, this.businessDate, monitorId);

        if (validStageRecords.isEmpty()) {
            logger.error("     [X] FAILED: Staging records exist, but none are eligible AND match mappings.");
            reportRow.put("Sub-Category Status", "FAILED - Stage mappings mismatch or not eligible");
            return false;
        }

        logger.info("     [✓] PASSED: Found {} perfectly matched & eligible records.", validStageRecords.size());
        record.put("VALID_STAGE_RECORDS", validStageRecords);
        reportRow.put("Sub-Category Status", "PASSED");
        return true;
    }
