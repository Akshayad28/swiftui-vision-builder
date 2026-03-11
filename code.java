@After
public void closeConnection(Scenario scenario) {

    try {

        if (preprodConn != null) {
            preprodConn.close();
        }

        if (prodConn != null) {
            prodConn.close();
        }

        isDBConnected = false;

        // Pass workbook and scenario name
        ExcelWriteClass.writeData(workbook, scenario.getName());

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
