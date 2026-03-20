  private static String extractOutlineId(Scenario scenario) {
        try {
            String id = scenario.getId();
            // e.g. "features/mytest.feature:15:23" → "features/mytest.feature:15"
            int lastColon = id.lastIndexOf(":");
            if (lastColon > 0) {
                return id.substring(0, lastColon);
            }
            return id;
        } catch (Exception e) {
            return "UnknownOutlineId";
        }
    }

    /**
     * Gets a clean name for the Excel filename from the scenario name.
     * Scenario Outline names in Cucumber look like:
     * "To verify completeness, get count for monitor=Monitor-India"
     * We take only the part before the first param (before "=")
     */
    private static String extractOutlineName(Scenario scenario) {
        try {
            String rawName = scenario.getName();
            if (rawName == null || rawName.trim().isEmpty()) {
                return "UnknownScenario";
            }
            // Split on first occurrence of a word followed by "="
            // e.g. "To verify completeness monitor=Monitor-India" 
            //   -> "To verify completeness"
            String[] parts = rawName.split("\\s\\S+=");
            return parts[0].trim();
        } catch (Exception e) {
            return "UnknownScenario";
        }
    }

    @Before
    public void setup(Scenario scenario) {
        this.testScenario = scenario;

        String incomingOutlineId   = extractOutlineId(scenario);
        String incomingOutlineName = extractOutlineName(scenario);

        System.out.println("==================================");
        System.out.println("Scenario ID      : " + scenario.getId());
        System.out.println("Outline ID       : " + incomingOutlineId);
        System.out.println("Outline Name     : " + incomingOutlineName);
        System.out.println("==================================");

        // ✅ Outline has changed — all examples of previous outline are done
        // Write Excel immediately for the completed outline
        if (currentOutlineId != null
                && !currentOutlineId.equals(incomingOutlineId)) {

            System.out.println("Outline [" + currentOutlineName 
                    + "] completed — writing Excel now...");

            ExcelWriteClass.writeData(currentOutlineName);  // ✅ write immediately
            ExcelWriteClass.workbook = null;                 // ✅ reset for next outline

            System.out.println("Excel written for: " + currentOutlineName);
        }

        // ✅ Initialize fresh workbook for new outline (or very first outline)
        if (ExcelWriteClass.workbook == null) {
            ExcelWriteClass.workbook = new XSSFWorkbook();
            currentOutlineId   = incomingOutlineId;
            currentOutlineName = incomingOutlineName;
        }

        // ✅ Open DB connections only once — reuse across all outlines
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
        // Just log — data already written to workbook by step definitions
        System.out.println("Example done: " + scenario.getName() 
                + " | Status: " + scenario.getStatus());
        scenario.log("Example completed: " + scenario.getName());
        // ✅ Do NOT write Excel here
        // ✅ Do NOT close DB here
    }

    @AfterAll
    public static void tearDownAll() {
        // ✅ Write Excel for the LAST outline
        // (not caught by @Before since no "next outline" comes after it)
        if (ExcelWriteClass.workbook != null) {
            System.out.println("Last outline [" + currentOutlineName 
                    + "] completed — writing Excel now...");
            ExcelWriteClass.writeData(currentOutlineName);
            ExcelWriteClass.workbook = null;
            currentOutlineName = null;
            currentOutlineId   = null;
        }

        // ✅ Close DB connections ONCE at the very end
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
