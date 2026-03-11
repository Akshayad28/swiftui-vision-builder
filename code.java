import org.apache.poi.ss.usermodel.*;

public String getCellValue(String columnName, int rowNum) {

    Row row = worksheet.getRow(rowNum);
    Cell cell = row.getCell(colByName.get(columnName));

    if (cell == null) {
        return "";
    }

    switch (cell.getCellType()) {

        case NUMERIC:
            return String.valueOf(cell.getNumericCellValue());

        case STRING:
            return cell.getStringCellValue();

        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());

        case BLANK:
            return "";

        case ERROR:
            return String.valueOf(cell.getErrorCellValue());

        case FORMULA:
            return String.valueOf(cell.getNumericCellValue());

        default:
            return "";
    }
}
