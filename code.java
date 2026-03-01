public List<String> getMonitorSedolValue(Connection connection, String monitor) throws DBExceptions {

    List<String> sedolList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;

    String query = "SELECT * FROM your_table WHERE MONITOR = ?";

    try {

        ps = connection.prepareStatement(query);
        ps.setString(1, monitor);

        rs = ps.executeQuery();

        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();

        // FIND SEDOL COLUMN (case insensitive)
        int sedolIndex = -1;
        for (int i = 1; i <= colCount; i++) {
            if (md.getColumnName(i).equalsIgnoreCase("SEDOL")) {
                sedolIndex = i;
                break;
            }
        }

        if (sedolIndex == -1) {
            throw new RuntimeException("SEDOL column NOT FOUND in query result");
        }

        while (rs.next()) {
            sedolList.add(rs.getString(sedolIndex));
        }

    } catch (Exception e) {
        throw new DBExceptions(e);

    } finally {
        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
        try { if (ps != null) ps.close(); } catch (Exception ignore) {}
    }

    return sedolList;
}
