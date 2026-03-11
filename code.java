public static void writeData(String scenarioName) {

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String timeDate = sdf.format(now);

    try {

        // Base path
        String basePath = System.getProperty("user.dir") + "/src/test/resources/excelfiles/";

        // Create scenario folder
        File scenarioFolder = new File(basePath + scenarioName);

        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }

        // Excel file inside scenario folder
        File file = new File(
                scenarioFolder + "/OracleTestResults_" + timeDate + ".xlsx"
        );

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            workbook.write(outputStream);

            System.out.println("Excel file written successfully at: " + file.getAbsolutePath());
        }

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
