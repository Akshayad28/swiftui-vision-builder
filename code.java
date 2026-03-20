@Before
public void setup(Scenario scenario) {
    this.testScenario = scenario;
    workbook = new XSSFWorkbook();   // fresh workbook per scenario ✓
    if (!isDBConnected) {
        try {
            /* String UAT_DB_Password = Security.decrypt(UAT_DB_PASSWORD);
               UATDbSql = new DBSQL(UAT_DbURL, UAT_DB_USERNAME, UAT_DB_Password); */

            String Preprod_DB_Password = Security.decrypt(DBPassword);
            preprodDbSql = new DBSQL(DbURL, DBUser, Preprod_DB_Password);
            preprodConn = preprodDbSql.getConnection();
            testScenario.log("PreProd connection is successfully connected.");

            String Prod_DB_Password = Security.decrypt(PROD_DB_PASSWORD);
            prodDbSql = new DBSQL(PROD_DbURL, PROD_DB_USERNAME, Prod_DB_Password);
            prodConn = prodDbSql.getConnection();
            testScenario.log("Prod connection is successfully connected.");

            // uatConn = UATDbSql.getConnection();
            isDBConnected = true;
        } catch (SQLException e) {
            throw new DBExceptions(e);
        }
    }
}

// ✅ Changed from @AfterAll (once) → @After (after EACH scenario)
// ✅ Changed from static → instance method so testScenario is accessible
@After
public void tearDown(Scenario scenario) {
    // Write a separate Excel file for THIS scenario before closing connections
    ExcelWriteClass.writeData(scenario.getName());
    System.out.println("Hooks After Each Scenario Called");

    try {
        preprodConn.close();
        System.out.println("Preprod connection is closed");
        scenario.log("Preprod connection is closed");

        prodConn.close();
        System.out.println("Prod connection is closed");
        scenario.log("Prod connection is closed");

        isDBConnected = false;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}



public static void writeData(String scenarioName) {
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss-SSS");
    String timeDate = sdf.format(now);

    // Sanitize scenario name so it's safe to use in a filename
    String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");

    File file = new File(
        System.getProperty("user.dir")
        + "/src/test/resources/excelfiles/"
        + "OracleTestResults_"
        + safeName + "_"
        + timeDate
        + ".xlsx"
    );

    try (FileOutputStream outputStream = new FileOutputStream(file)) {
        workbook.write(outputStream);
        System.out.println("Excel file written successfully: " + file.getName());
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
