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

            if (row != null) {

                Cell cell = row.getCell(0);

                if (cell != null) {

                    monitorList.add(cell.getStringCellValue().trim());
                }
            }
        }

        workbook.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return monitorList;
}
