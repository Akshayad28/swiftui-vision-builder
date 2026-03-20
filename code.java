public static void writeData(String tagName) {

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
    String timeDate = sdf.format(now);

    FileOutputStream outputStream = null;

    try {

        String basePath = System.getProperty("user.dir") + "/src/test/resources/excelfiles/";

        // ✅ Default fallback
        if (tagName == null || tagName.isEmpty()) {
            tagName = "DefaultTag";
        }

        tagName = tagName.replaceAll("[\\\\/:*?\"<>|]", "_");

        // ✅ Create folder using TAG
        File tagFolder = new File(basePath + tagName);

        if (!tagFolder.exists()) {
            tagFolder.mkdirs();
        }

        // ✅ Create Excel inside tag folder
        File file = new File(
                tagFolder + "/OracleTestResults_" + timeDate + ".xlsx"
        );

        outputStream = new FileOutputStream(file);

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

    // ✅ Reset workbook (since per scenario file)
    workbook = new XSSFWorkbook();
}
