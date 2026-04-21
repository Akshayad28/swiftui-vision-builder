
package utils;

import java.sql.*;
import java.util.*;

public class DatabaseResultSet {

    private List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    public List<Map<String, Object>> getTopAggrRecords(Connection conn, int limit, String level) throws SQLException {
        String query = "SELECT * FROM lhone_aggr_pos WHERE AGGREGATION_LEVEL = ? FETCH FIRST ? ROWS ONLY";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, level);
            pstmt.setInt(2, limit);
            return convertResultSetToList(pstmt.executeQuery());
        }
    }

    public List<Map<String, Object>> getStagingRecords(Connection conn, String lei, String parentEntity) throws SQLException {
        String query = "SELECT LEI, SDS_ENTITY_FIRST_L, HOLDING_CLASSIFICATION, FILE_ID, LANDING_REC_ID " +
                       "FROM lhone_stage_pos_copy WHERE LEI = ? AND SDS_ENTITY_FIRST_L = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, lei);
            pstmt.setString(2, parentEntity);
            return convertResultSetToList(pstmt.executeQuery());
        }
    }

    public boolean checkEligibility(Connection conn, String fileId, String landingRecId, int monitorId) throws SQLException {
        String query = "SELECT 1 FROM lhone_europe_eligibility_pos WHERE FILE_ID = ? AND LANDING_REC_ID = ? AND MONITOR_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fileId);
            pstmt.setString(2, landingRecId);
            pstmt.setInt(3, monitorId);
            return pstmt.executeQuery().next();
        }
    }

    public boolean validateCategoryFallback(Connection conn, Set<String> holdingClasses) throws SQLException {
        if (holdingClasses == null || holdingClasses.isEmpty()) return false;
        String placeholders = String.join(",", Collections.nCopies(holdingClasses.size(), "?"));
        String query = "SELECT 1 FROM lhone_monitor_rpt_cat WHERE REPORT_CATEGORY IN (" + placeholders + ")";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            int index = 1;
            for (String hc : holdingClasses) pstmt.setString(index++, hc);
            return pstmt.executeQuery().next(); 
        }
    }

    public boolean isSubCategoryValid(Connection conn, String subCategory) throws SQLException {
        String query = "SELECT 1 FROM lhone_monitor_rpt_sub_cat WHERE SUB_REPORT_CATEGORY = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, subCategory);
            return pstmt.executeQuery().next();
        }
    }

    public Map<String, Set<String>> getProductMappings(Connection conn, String subCategory) throws SQLException {
        String query = "SELECT PRODUCT_TYPE, PRODUCT_SUB_TYPE, SETTLEMENT_TYPE FROM lhone_product_map " +
                       "WHERE UK_LONG_CLASSIFICATION = ? AND UK_LONG_SCOPE = 'Y'";
        
        Map<String, Set<String>> mappings = new HashMap<>();
        mappings.put("PRODUCT_TYPE", new HashSet<>());
        mappings.put("PRODUCT_SUB_TYPE", new HashSet<>());
        mappings.put("SETTLEMENT_TYPE", new HashSet<>());

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, subCategory);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("PRODUCT_TYPE") != null) mappings.get("PRODUCT_TYPE").add(rs.getString("PRODUCT_TYPE"));
                if (rs.getString("PRODUCT_SUB_TYPE") != null) mappings.get("PRODUCT_SUB_TYPE").add(rs.getString("PRODUCT_SUB_TYPE"));
                if (rs.getString("SETTLEMENT_TYPE") != null) mappings.get("SETTLEMENT_TYPE").add(rs.getString("SETTLEMENT_TYPE"));
            }
        }
        return mappings;
    }

    private String buildInClausePlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    // UPDATED: Fetches LONG_SHORT_INDICATOR, DELTA_UL_QUANTITY, and ISSUER_VOTING_RIGHTS_OUTSTANDING
    public List<Map<String, Object>> getStagingRecordsByProductMap(
            Connection conn, String lei, String parentEntity, Map<String, Set<String>> mappings) throws SQLException {
        
        Set<String> pTypes = mappings.get("PRODUCT_TYPE");
        Set<String> sTypes = mappings.get("PRODUCT_SUB_TYPE");
        Set<String> setTypes = mappings.get("SETTLEMENT_TYPE");

        if (pTypes.isEmpty() || sTypes.isEmpty() || setTypes.isEmpty()) return new ArrayList<>();

        String query = "SELECT LONG_SHORT_INDICATOR, DELTA_UL_QUANTITY, ISSUER_VOTING_RIGHTS_OUTSTANDING " +
                       "FROM lhone_stage_pos_copy " +
                       "WHERE LEI = ? AND SDS_ENTITY_FIRST_L = ? " +
                       "AND PRODUCT_TYPE IN (" + buildInClausePlaceholders(pTypes.size()) + ") " +
                       "AND PRODUCT_SUB_TYPE IN (" + buildInClausePlaceholders(sTypes.size()) + ") " +
                       "AND SETTLEMENT_TYPE IN (" + buildInClausePlaceholders(setTypes.size()) + ")";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            int index = 1;
            pstmt.setString(index++, lei);
            pstmt.setString(index++, parentEntity);

            for (String type : pTypes) pstmt.setString(index++, type);
            for (String subType : sTypes) pstmt.setString(index++, subType);
            for (String settleType : setTypes) pstmt.setString(index++, settleType);

            return convertResultSetToList(pstmt.executeQuery());
        }
    }
}
