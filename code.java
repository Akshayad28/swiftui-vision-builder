public static void writeData(String scenarioName) {

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
    String timeDate = sdf.format(now);

    FileOutputStream outputStream = null;

    try {

        String basePath = System.getProperty("user.dir") + "/src/test/resources/excelfiles/";

        File scenarioFolder = new File(basePath + scenarioName);

        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }

        File file = new File(
                scenarioFolder + "/OracleTestResults_" + timeDate + ".xlsx"
        );

        outputStream = new FileOutputStream(file);

        // ✅ This writes FULL workbook (all sheets)
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

    // ❌ REMOVE THIS LINE (VERY IMPORTANT)
    // workbook = new XSSFWorkbook();
}
