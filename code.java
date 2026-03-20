private static Map<String, Integer> executionCount = new HashMap<>();
private static Map<String, Integer> lastSeenCount = new HashMap<>();


@Before
public void beforeScenario(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    int count = executionCount.getOrDefault(scenarioId, 0) + 1;
    executionCount.put(scenarioId, count);

    lastSeenCount.put(scenarioId, count);
}


@After
public void afterScenario(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    int executed = executionCount.get(scenarioId);
    int latest = lastSeenCount.get(scenarioId);

    // ✅ ONLY LAST EXECUTION OF THIS SCENARIO OUTLINE
    if (executed == latest) {

        ExcelWriteClass.writeData();   // ✅ write once

        System.out.println("📊 Excel created for scenario: " + scenarioId);

        // 🔥 reset workbook for next scenario
        ExcelWriteClass.resetWorkbook();

        executionCount.remove(scenarioId);
        lastSeenCount.remove(scenarioId);
    }
}


public static Workbook workbook = new XSSFWorkbook();

public static void writeData() {

    FileOutputStream outputStream = null;

    try {

        // ✅ Timestamp
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        // ✅ File path
        String filePath = System.getProperty("user.dir")
                + "/src/test/resources/excelfiles/"
                + "OracleTestResults_" + timeDate + ".xlsx";

        File file = new File(filePath);

        // ✅ Write workbook
        outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        System.out.println("✅ Excel written successfully: " + file.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
    } finally {

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔥 IMPORTANT → Reset workbook for next scenario
        workbook = new XSSFWorkbook();
    }
}
