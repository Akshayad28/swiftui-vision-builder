public static void writeData() {

    FileOutputStream outputStream = null;

    try {

        // 👉 Generate timestamp (same as your code)
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        // 👉 File path
        File file = new File(
                System.getProperty("user.dir")
                        + "/src/test/resources/excelfiles/"
                        + "OracleTestResults_" + timeDate + ".xlsx"
        );

        // 👉 Write workbook (NO RESET)
        outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        System.out.println("✅ Excel file written successfully at: " + file.getAbsolutePath());

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

        // ❗ IMPORTANT: DO NOT close workbook here
        // workbook.close(); ❌ REMOVE THIS LINE
    }
}
