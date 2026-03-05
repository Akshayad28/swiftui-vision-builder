String baseSheetName = monitor.replaceAll("[\\\\/?*\\[\\]]", "_");

// Excel sheet name limit = 31 characters
if (baseSheetName.length() > 25) {
    baseSheetName = baseSheetName.substring(0, 25);
}

String sheetName = baseSheetName;
int counter = 1;

// Ensure unique sheet name
while (ExcelWriteClass.workbook.getSheet(sheetName) != null) {
    sheetName = baseSheetName + "_" + counter;
    counter++;
}

excelWriteClass.writeSedolRows(
        sheetName,
        headers,
        excelRows
);
