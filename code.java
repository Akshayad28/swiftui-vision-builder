 private static String getTagKey(Scenario scenario) {
        List<String> sortedTags = new ArrayList<>(scenario.getSourceTagNames());
        Collections.sort(sortedTags);
        return String.join("_", sortedTags);
    }

    // ✅ Used only for Excel filename
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

        String currentTagKey     = getTagKey(scenario);
        String currentOutlineName = getOutlineName(scenario);

        System.out.println("Tag Key  : " + currentTagKey);
        System.out.println("Outline  : " + currentOutlineName);

        if (previousTagKey == null) {
            // ✅ Very first example — create workbook
            ExcelWriteClass.workbook = new XSSFWorkbook();
            previousTagKey      = currentTagKey;
            previousOutlineName = currentOutlineName;

        } else if (!previousTagKey.equals(currentTagKey)) {
            // ✅ Tags changed = new outline started
            // Write Excel for PREVIOUS outline immediately
            System.out.println("New outline detected! Writing Excel for: " 
                + previousOutlineName);
            ExcelWriteClass.writeData(previousOutlineName);

            // Create fresh workbook for new outline
            ExcelWriteClass.workbook = new XSSFWorkbook();
            previousTagKey      = currentTagKey;
            previousOutlineName = currentOutlineName;
        }
        // else — same outline, same tags, reuse workbook ✅

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
        // ✅ Write Excel for last outline
        if (ExcelWriteClass.workbook != null && previousOutlineName != null) {
            System.out.println("Writing Excel for last outline: " 
                + previousOutlineName);
            ExcelWriteClass.writeData(previousOutlineName);
        }

        // ✅ Close DB once at very end
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
