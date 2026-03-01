public List<String> getMonitorSedolValue(Connection connection, String monitor) throws DBExceptions {

    List<String> sedolList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;

    // Always select all so metadata shows ALL columns Oracle returns
    String query = "SELECT * FROM your_table WHERE MONITOR = ?";

    try {

        ps = connection.prepareStatement(query);
        ps.setString(1, monitor);
        rs = ps.executeQuery();

        // Print columns actually returned to JDBC
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        System.out.println("=== JDBC Columns Returned ===");
        for (int i = 1; i <= colCount; i++) {
            System.out.println("COL " + i + ": " + meta.getColumnName(i));
        }

        // Find SEDOL column dynamically
        int sedolColumnIndex = -1;

        for (int i = 1; i <= colCount; i++) {
            if (meta.getColumnName(i).equalsIgnoreCase("SEDOL")) {
                sedolColumnIndex = i;
                break;
            }
        }

        if (sedolColumnIndex == -1) {
            throw new RuntimeException("SEDOL column NOT found in JDBC ResultSet.");
        }

        // Read ALL rows
        while (rs.next()) {
            sedolList.add(rs.getString(sedolColumnIndex));
        }

    } catch (SQLException e) {
        throw new DBExceptions(e);

    } finally {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    return sedolList;
}
