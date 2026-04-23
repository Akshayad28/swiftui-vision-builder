private boolean calculateAndValidateQuantityForRecord(Map<String, Object> record, Map<String, String> reportRow, boolean continueValidation) {
        if (!continueValidation || record.get("VALID_STAGE_RECORDS") == null) {
            reportRow.put("Quantity Status", "SKIPPED");
            return false;
        }

        List<Map<String, Object>> stageRecords = (List<Map<String, Object>>) record.get("VALID_STAGE_RECORDS");
        double totalUlQuantity = 0.0;
        int skippedCount = 0; // Track how many records get skipped for logging

        logger.info("  -> QUANTITY: Calculating total across {} valid staging records.", stageRecords.size());

        for (Map<String, Object> stageRec : stageRecords) {
            // NEW VALIDATION: Check if ISSUER_VOTING_RIGHTS_OUTSTANDING is empty or null
            Object votingRights = stageRec.get("ISSUER_VOTING_RIGHTS_OUTSTANDING");
            if (votingRights == null || String.valueOf(votingRights).trim().isEmpty()) {
                logger.warn("     [!] Skipping Row - ISSUER_VOTING_RIGHTS_OUTSTANDING is empty or null.");
                skippedCount++;
                continue; // Skips the addition/subtraction and moves to the next record
            }

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
        
        logger.info("     Validation -> Expected: {}, Calculated: {} (Skipped {} invalid records)", expectedHoldings, totalUlQuantity, skippedCount);

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
