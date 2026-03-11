public static void writeData(Workbook workbook, String scenarioName) {

    try {

        // Remove spaces & special characters
        scenarioName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_");

        // Create timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        // Base folder
        String basePath = System.getProperty("user.dir")
                + "/src/test/resources/excelfiles/";

        // Scenario folder path
        String scenarioFolderPath = basePath + scenarioName;

        File scenarioFolder = new File(scenarioFolderPath);

        // Create folder if not exists
        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }

        // Excel file name
        String fileName = "OracleTestResults_" + timestamp + ".xlsx";

        File file = new File(scenarioFolderPath + "/" + fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            workbook.write(outputStream);
            workbook.close();

            System.out.println("Excel written at: " + file.getAbsolutePath());

        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
