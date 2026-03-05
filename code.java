@When("Get sedol values for monitors from excel and TCID={string}")
public void getSedolCountForMonitorComplete(String TCID) throws SQLException {

    List<String> monitorList = excelWriteClass.getMonitorNamesFromExcel();

    for (String monitor : monitorList) {

        System.out.println("TestCase " + TCID + " execution starts for monitor: " + monitor);
        testScenario.log("TestCase " + TCID + " execution starts for monitor: " + monitor);

        preProdSedolList =
                databaseResultSet.getMonitorSedolValue(
                        Hooks.preprodConn,
                        monitor.replace("%", "%%")
                );

        System.out.println("Monitor Name in preprod: " + monitor);
        testScenario.log("Monitor Name in preprod: " + monitor);

        prodSedolList =
                databaseResultSet.getMonitorSedolValue(
                        Hooks.prodConn,
                        monitor.replace("%", "%%")
                );

        System.out.println("Monitor Name in Prod: " + monitor);
        testScenario.log("Monitor Name in Prod: " + monitor);

        // Run comparison step manually
        compareSedolList(monitor);
    }
}
