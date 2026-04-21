package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelReportGenerator {

    public static void generateReport(List<Map<String, String>> testResults, String fileName) {
        if (testResults == null || testResults.isEmpty()) return;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Validation Results");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        Set<String> columns = testResults.get(0).keySet();
        int colNum = 0;
        for (String colName : columns) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(colName);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Map<String, String> result : testResults) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;
            for (String colName : columns) {
                row.createCell(colNum++).setCellValue(result.getOrDefault(colName, ""));
            }
        }

        for (int i = 0; i < columns.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            FileOutputStream fileOut = new FileOutputStream("target/" + fileName + "_" + timestamp + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            System.out.println("Excel Validation Report Generated: target/" + fileName + "_" + timestamp + ".xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
