package com.naenae.common.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelFileService {

    public byte[] createTemplate(String sheetName, List<String> headers, List<String> sampleValues) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            writeRow(sheet.createRow(0), headers);
            if (sampleValues != null && !sampleValues.isEmpty()) {
                writeRow(sheet.createRow(1), sampleValues);
            }
            for (int index = 0; index < headers.size(); index++) {
                sheet.autoSizeColumn(index);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("엑셀 템플릿을 생성할 수 없습니다.", exception);
        }
    }

    public List<ExcelRow> readRows(MultipartFile file, List<String> expectedHeaders) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 엑셀 파일을 선택해 주세요.");
        }
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet, expectedHeaders, formatter);
            List<ExcelRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                List<String> values = readValues(row, expectedHeaders.size(), formatter);
                if (values.stream().allMatch(value -> value == null)) {
                    continue;
                }
                rows.add(new ExcelRow(rowIndex + 1, values));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("엑셀 파일을 읽을 수 없습니다.", exception);
        }
    }

    private void validateHeaders(Sheet sheet, List<String> expectedHeaders, DataFormatter formatter) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("엑셀 첫 줄에 헤더가 필요합니다.");
        }
        List<String> actualHeaders = readValues(header, expectedHeaders.size(), formatter);
        if (!expectedHeaders.equals(actualHeaders)) {
            throw new IllegalArgumentException("엑셀 헤더는 '" + String.join(", ", expectedHeaders) + "' 순서여야 합니다.");
        }
    }

    private List<String> readValues(Row row, int columnCount, DataFormatter formatter) {
        List<String> values = new ArrayList<>(columnCount);
        for (int index = 0; index < columnCount; index++) {
            values.add(readCell(row, index, formatter));
        }
        return values;
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeRow(Row row, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            row.createCell(index).setCellValue(values.get(index));
        }
    }

    public record ExcelRow(int rowNumber, List<String> values) {
        public String value(int index) {
            return values.get(index);
        }
    }
}