public static void writeData(String outlineName) {

    // null guard
    if (outlineName == null || outlineName.trim().isEmpty()) {
        outlineName = "UnknownScenario";
    }

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
        System.out.println("Excel file written: " + file.getName());
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
