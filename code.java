public static void writeData(String scenarioName) {

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
    String timeDate = sdf.format(now);

    FileOutputStream outputStream = null;

    try {

        String basePath = System.getProperty("user.dir") + "/src/test/resources/excelfiles/";

        // ✅ Clean scenario name (important)
        if (scenarioName == null || scenarioName.trim().isEmpty()) {
            scenarioName = "DefaultScenario";
        }

        scenarioName = scenarioName
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // remove invalid chars
                .trim();

        // ✅ Create folder with scenario name
        File scenarioFolder = new File(basePath + scenarioName);

        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }

        // ✅ Create file
        File file = new File(
                scenarioFolder + "/OracleTestResults_" + timeDate + ".xlsx"
        );

        outputStream = new FileOutputStream(file);

        // ✅ Write FULL workbook (all sheets preserved)
        workbook.write(outputStream);

        System.out.println("✅ Excel written at: " + file.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (outputStream != null) outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ❌ REMOVE THIS LINE (CRITICAL FIX)
    // workbook = new XSSFWorkbook();
}
