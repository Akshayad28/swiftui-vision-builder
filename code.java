public static void writeData(String tagName) {

    FileOutputStream outputStream = null;

    try {

        // 👉 Generate timestamp
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        // 👉 Default tag
        if (tagName == null || tagName.trim().isEmpty()) {
            tagName = "DefaultTag";
        }

        // 👉 Clean tag name
        tagName = tagName.replaceAll("[\\\\/:*?\"<>|]", "_");

        // 👉 Base path
        String basePath = System.getProperty("user.dir")
                + "/src/test/resources/excelfiles/";

        // 👉 Create tag folder
        File tagFolder = new File(basePath + tagName);

        if (!tagFolder.exists()) {
            tagFolder.mkdirs();
        }

        // 👉 Create file INSIDE tag folder
        File file = new File(
                tagFolder + "/OracleTestResults_" + timeDate + ".xlsx"
        );

        // 👉 Write workbook (same as before)
        outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        System.out.println("✅ Excel file written at: " + file.getAbsolutePath());

    } catch (IOException e) {
        e.printStackTrace();
    } finally {

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ❗ DO NOT close workbook
    }
}
