public class Hooks {

    private static XSSFWorkbook workbook;
    private static String currentOutlineName;   // tracks which outline is running
    private static boolean isDBConnected = false;

    private static Connection preprodConn;
    private static Connection prodConn;
    private static DBSQL preprodDbSql;
    private static DBSQL prodDbSql;

    private Scenario testScenario;

    // Extracts base outline name by stripping parameterized values
    // e.g. "To verify completeness monitor=Monitor-India TCID=MLTC001"
    //   -> "To verify completeness"
    private static String extractOutlineName(Scenario scenario) {
        // Cucumber scenario outline names contain the example values appended
        // We use the URI + line-number group OR simply the first part before any param pattern
        // Simplest reliable way: use the feature URI + scenario name up to first param
        String rawName = scenario.getName();
        // Strip anything after the first example parameter pattern if present
        // Adjust this split logic based on how your scenario names look
        return rawName.replaceAll("\\s*[A-Z].*=.*", "").trim();
    }

    @Before
    public void setup(Scenario scenario) {
        this.testScenario = scenario;

        String incomingOutlineName = extractOutlineName(scenario);

        // ✅ Detect outline change — flush previous workbook and start fresh
        if (currentOutlineName != null 
                && !currentOutlineName.equals(incomingOutlineName)) {
            
            System.out.println("New outline detected — writing Excel for: " 
                + currentOutlineName);
            
            // Write Excel for the PREVIOUS outline before resetting
            ExcelWriteClass.writeData(workbook, currentOutlineName);
            workbook = null;  // reset for new outline
        }

        // ✅ Initialize workbook for new outline (or first outline)
        if (workbook == null) {
            workbook = new XSSFWorkbook();
            currentOutlineName = incomingOutlineName;
        }

        // Open DB connections only once across all scenarios
        if (!isDBConnected) {
            try {
                String Preprod_DB_Password = Security.decrypt(DBPassword);
                preprodDbSql = new DBSQL(DbURL, DBUser, Preprod_DB_Password);
                preprodConn = preprodDbSql.getConnection();
                testScenario.log("PreProd connection is successfully connected.");

                String Prod_DB_Password = Security.decrypt(PROD_DB_PASSWORD);
                prodDbSql = new DBSQL(PROD_DbURL, PROD_DB_USERNAME, Prod_DB_Password);
                prodConn = prodDbSql.getConnection();
                testScenario.log("Prod connection is successfully connected.");

                isDBConnected = true;
            } catch (SQLException e) {
                throw new DBExceptions(e);
            }
        }
    }

    @After
    public void afterEachScenario(Scenario scenario) {
        // Just log — workbook is being populated by step definitions
        System.out.println("Example completed: " + scenario.getName());
        scenario.log("Example completed: " + scenario.getName());
        // ✅ No file write here, no DB close here
    }

    @AfterAll
    public static void tearDownAll() {
        // ✅ Write Excel for the LAST outline (not caught by @Before transition)
        if (workbook != null) {
            ExcelWriteClass.writeData(workbook, currentOutlineName);
            workbook = null;
            currentOutlineName = null;
        }

        // ✅ Close DB connections once at the very end
        try {
            if (preprodConn != null && !preprodConn.isClosed()) {
                preprodConn.close();
                System.out.println("Preprod connection is closed");
            }
            if (prodConn != null && !prodConn.isClosed()) {
                prodConn.close();
                System.out.println("Prod connection is closed");
            }
            isDBConnected = false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

**How it works for your case (2 outlines × 5 examples):**
```
Outline 1 - Example 1  →  @Before: workbook created, outlineName = "Outline1"
Outline 1 - Example 2  →  @Before: same outline, reuse workbook
Outline 1 - Example 3  →  @Before: same outline, reuse workbook
Outline 1 - Example 4  →  @Before: same outline, reuse workbook
Outline 1 - Example 5  →  @Before: same outline, reuse workbook

Outline 2 - Example 1  →  @Before: outline changed! ✅ WRITE Outline1.xlsx
                                    new workbook created, outlineName = "Outline2"
Outline 2 - Example 2  →  @Before: same outline, reuse workbook
Outline 2 - Example 3  →  @Before: same outline, reuse workbook
Outline 2 - Example 4  →  @Before: same outline, reuse workbook
Outline 2 - Example 5  →  @Before: same outline, reuse workbook

@AfterAll              →  ✅ WRITE Outline2.xlsx  (last outline always written here)
