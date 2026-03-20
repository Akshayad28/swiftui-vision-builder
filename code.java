public static void writeData(String tagName) {

    try {

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String timeDate = sdf.format(now);

        String basePath = System.getProperty("user.dir") + "/src/test/resources/excelfiles/";

        // ✅ Create folder using tag
        File folder = new File(basePath + tagName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // ✅ Create file inside that folder
        File file = new File(folder + "/OracleTestResults_" + timeDate + ".xlsx");

        FileOutputStream outputStream = new FileOutputStream(file);

        workbook.write(outputStream);
        outputStream.close();

        System.out.println("✅ Excel written at: " + file.getAbsolutePath());

        // 🔥 RESET workbook for next scenario
        workbook = new XSSFWorkbook();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
