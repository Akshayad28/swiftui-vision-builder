public static void writeData() {

    FileOutputStream outputStream = null;

    try {

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        // 👉 Use tag from Hooks
        String tagName = Hooks.tagName;

        if (tagName == null || tagName.isEmpty()) {
            tagName = "default";
        }

        String fileName = "OracleTestResults_" + timeDate + "_" + tagName + ".xlsx";

        File file = new File(
                System.getProperty("user.dir")
                        + "/src/test/resources/excelfiles/"
                        + fileName
        );

        outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        System.out.println("✅ Excel file written: " + file.getAbsolutePath());

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
