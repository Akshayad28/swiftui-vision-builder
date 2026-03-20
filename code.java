public class Hooks {

    // ✅ Map stores one workbook per outline tag — simple and reliable
    public static Map<String, XSSFWorkbook> workbookMap = new LinkedHashMap<>();
    
    private static boolean isDBConnected = false;
    private static Connection preprodConn;
    private static Connection prodConn;
    private Scenario testScenario;

    private static String getTagKey(Scenario scenario) {
        List<String> sortedTags = new ArrayList<>(scenario.getSourceTagNames());
        Collections.sort(sortedTags);
        return String.join("_", sortedTags);
    }

    private static String getOutlineName(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (!tag.startsWith("@XT-")) {
                return tag.replace("@", "").trim();
            }
        }
        return "UnknownScenario";
    }

    @Before
    public void setup(Scenario scenario) {
        this.testScenario = scenario;

        String tagKey       = getTagKey(scenario);
        String outlineName  = getOutlineName(scenario);

        System.out.println("Tag Key      : " + tagKey);
        System.out.println("Outline Name : " + outlineName);

        // ✅ Create workbook for this outline ONLY if it doesn't exist yet
        // Same outline = same tagKey = reuse existing workbook
        if (!workbookMap.containsKey(tagKey)) {
            System.out.println("Creating new workbook for : " + outlineName);
            workbookMap.put(tagKey, new XSSFWorkbook());
        }

        // ✅ Always point ExcelWriteClass.workbook to correct workbook
        ExcelWriteClass.workbook = workbookMap.get(tagKey);

        // Open DB connections only once
        if (!isDBConnected) {
            try {
                String Preprod_DB_Password = Security.decrypt(DBPassword);
                preprodDbSql = new DBSQL(DbURL, DBUser, Preprod_DB_Password);
                preprodConn  = preprodDbSql.getConnection();
                testScenario.log("PreProd connection successfully connected.");

                String Prod_DB_Password = Security.decrypt(PROD_DB_PASSWORD);
                prodDbSql = new DBSQL(PROD_DbURL, PROD_DB_USERNAME, Prod_DB_Password);
                prodConn  = prodDbSql.getConnection();
                testScenario.log("Prod connection successfully connected.");

                isDBConnected = true;
            } catch (SQLException e) {
                throw new DBExceptions(e);
            }
        }
    }

    @After
    public void afterEachExample(Scenario scenario) {
        System.out.println("Example done : " + scenario.getName()
                + " | Status : " + scenario.getStatus());
        // ✅ No Excel write here
        // ✅ No DB close here
    }

    @AfterAll
    public static void tearDownAll() {
        // ✅ Write ONE Excel file per outline — guaranteed
        for (Map.Entry<String, XSSFWorkbook> entry : workbookMap.entrySet()) {
            String tagKey      = entry.getKey();
            XSSFWorkbook wb    = entry.getValue();

            // Get outline name from tagKey 
            // tagKey = "@XT-LH-3627_@XT-LH-3635_@monitorCompleteness"
            String outlineName = "UnknownScenario";
            for (String tag : tagKey.split("_")) {
                if (!tag.startsWith("@XT-")) {
                    outlineName = tag.replace("@", "").trim();
                    break;
                }
            }

            ExcelWriteClass.workbook = wb;
            ExcelWriteClass.writeData(outlineName);
            System.out.println("Excel written for : " + outlineName);
        }

        workbookMap.clear();

        // ✅ Close DB once at the very end
        try {
            if (preprodConn != null && !preprodConn.isClosed()) {
                preprodConn.close();
                System.out.println("Preprod connection closed.");
            }
            if (prodConn != null && !prodConn.isClosed()) {
                prodConn.close();
                System.out.println("Prod connection closed.");
            }
            isDBConnected = false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
