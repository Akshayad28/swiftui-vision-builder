// UPDATED: Now performs an INNER JOIN to eligibility to ensure ONLY eligible records are counted
    public List<Map<String, Object>> getStagingRecordsByProductMap(
            Connection conn, String lei, String parentEntity, Map<String, Set<String>> mappings, 
            int jobId, String businessDate, int monitorId) throws SQLException {
        
        Set<String> pTypes = mappings.get("PRODUCT_TYPE");
        Set<String> sTypes = mappings.get("PRODUCT_SUB_TYPE");
        Set<String> setTypes = mappings.get("SETTLEMENT_TYPE");

        if (pTypes.isEmpty() || sTypes.isEmpty() || setTypes.isEmpty()) return new ArrayList<>();

        String query = "SELECT s.LONG_SHORT_INDICATOR, s.DELTA_UL_QUANTITY, s.ISSUER_VOTING_RIGHTS_OUTSTANDING " +
                       "FROM lhone_stage_pos_copy s " +
                       "INNER JOIN lhone_europe_eligibility_pos e " +
                       "  ON s.FILE_ID = e.FILE_ID AND s.LANDING_REC_ID = e.LANDING_REC_ID " +
                       "WHERE s.LEI = ? AND s.SDS_ENTITY_FIRST_L = ? " +
                       "  AND s.JOB_ID = ? AND s.BUSINESS_DATE = ? AND e.MONITOR_ID = ? " +
                       "  AND s.PRODUCT_TYPE IN (" + buildInClausePlaceholders(pTypes.size()) + ") " +
                       "  AND s.PRODUCT_SUB_TYPE IN (" + buildInClausePlaceholders(sTypes.size()) + ") " +
                       "  AND s.SETTLEMENT_TYPE IN (" + buildInClausePlaceholders(setTypes.size()) + ")";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            int index = 1;
            pstmt.setString(index++, lei);
            pstmt.setString(index++, parentEntity);
            pstmt.setInt(index++, jobId);
            pstmt.setString(index++, businessDate);
            pstmt.setInt(index++, monitorId); // Dynamically binding the Monitor ID here

            for (String type : pTypes) pstmt.setString(index++, type);
            for (String subType : sTypes) pstmt.setString(index++, subType);
            for (String settleType : setTypes) pstmt.setString(index++, settleType);

            return convertResultSetToList(pstmt.executeQuery());
        }
    }
