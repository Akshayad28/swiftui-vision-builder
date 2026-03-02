public void writeSedolRows(String sheetName,
                           List<String> headers,
                           List<List<String>> rows,
                           String filePath) {

    Sheet sheet;

    // Create or get existing sheet
    if (workbook.getSheet(sheetName) == null) {
        sheet = workbook.createSheet(sheetName);

        // Create header row once
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

    } else {
        sheet = workbook.getSheet(sheetName);
    }

    // Append rows at the end
    int startRow = sheet.getLastRowNum() + 1;

    for (List<String> rowData : rows) {

        Row row = sheet.createRow(startRow++);
        int colNum = 0;

        // Write cells individually (fixes list printing issue)
        for (String cellValue : rowData) {
            Cell cell = row.createCell(colNum++);
            cell.setCellValue(cellValue);
        }
    }

    // Write file safely
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
        workbook.write(fos);
        fos.flush();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
