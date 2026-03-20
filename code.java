    @After
    public void afterEachScenario(Scenario scenario) {
        // ✅ Each example row's data gets written into the shared workbook
        // (Your step definitions should be adding sheets/rows to ExcelWriteClass.workbook)
        // Just log — do NOT write the file here
        System.out.println("Example completed: " + scenario.getName());
        scenario.log("Example completed: " + scenario.getName());

        // ✅ DO NOT close DB connections here — reuse them across examples
        // ✅ DO NOT write Excel here — wait for @AfterAll
    }

    @AfterAll
    public static void tearDownAll() {
        // ✅ Write ONE Excel file after ALL examples of the outline are done
        if (workbook != null) {
            ExcelWriteClass.writeData(workbook, outlineName);
            workbook = null;
            outlineName = null;
        }

        // ✅ Close DB connections once, after everything is done
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


public static void writeData(XSSFWorkbook workbook, String outlineName) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        String safeName = outlineName.replaceAll("[^a-zA-Z0-9_-]", "_");

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
