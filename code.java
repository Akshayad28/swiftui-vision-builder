public List<String> getMonitorNamesFromExcel() {

    List<String> monitorList = new ArrayList<>();

    try {

        FileInputStream fis = new FileInputStream(
                System.getProperty("user.dir")
                        + "/src/test/resources/excelfiles/MonitorNames_TestData.xlsx");

        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheet("TestData");

        int rowCount = sheet.getLastRowNum();

        for (int i = 1; i <= rowCount; i++) {

            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            Cell cell = row.getCell(0);

            if (cell == null) {
                continue;
            }

            String monitor = cell.toString().trim();

            // Skip empty monitor rows
            if (monitor.isEmpty()) {
                continue;
            }

            monitorList.add(monitor);
        }

        workbook.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return monitorList;
}
