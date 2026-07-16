package com.naenae.common.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelFileService {

    public byte[] createTemplate(String sheetName, List<String> headers, List<String> sampleValues) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            writeHeaderRow(workbook, sheet.createRow(0), headers);
            if (sampleValues != null && !sampleValues.isEmpty()) {
                writeRow(sheet.createRow(1), sampleValues);
            }
            finishTemplateSheet(sheet, headers.size(), List.of());
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("엑셀 템플릿을 생성할 수 없습니다.", exception);
        }
    }

    public byte[] createMultiSheetTemplate(List<ExcelSheetTemplate> sheets) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (ExcelSheetTemplate definition : sheets) {
                Sheet sheet = workbook.createSheet(definition.sheetName());
                writeHeaderRow(workbook, sheet.createRow(0), definition.headers());
                if (definition.sampleValues() != null && !definition.sampleValues().isEmpty()) {
                    writeRow(sheet.createRow(1), definition.sampleValues());
                }
                finishTemplateSheet(sheet, definition.headers().size(), definition.columnWidths());
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

    public Map<String, List<ExcelRow>> readSheets(MultipartFile file, Map<String, List<String>> expectedSheets) {
        validateFile(file);
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Map<String, List<ExcelRow>> result = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : expectedSheets.entrySet()) {
                Sheet sheet = workbook.getSheet(entry.getKey());
                if (sheet == null) throw new IllegalArgumentException("'" + entry.getKey() + "' 시트가 필요합니다.");
                validateHeaders(sheet, entry.getValue(), formatter);
                result.put(entry.getKey(), readDataRows(sheet, entry.getValue().size(), formatter));
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("엑셀 파일을 읽을 수 없습니다.", exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 엑셀 파일을 선택해 주세요.");
        }
    }

    private List<ExcelRow> readDataRows(Sheet sheet, int columnCount, DataFormatter formatter) {
        List<ExcelRow> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            List<String> values = readValues(row, columnCount, formatter);
            if (values.stream().allMatch(value -> value == null)) continue;
            rows.add(new ExcelRow(rowIndex + 1, values));
        }
        return rows;
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

    private void writeHeaderRow(Workbook workbook, Row row, List<String> values) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        row.setHeightInPoints(26);
        for (int index = 0; index < values.size(); index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(values.get(index));
            cell.setCellStyle(style);
        }
    }

    private void finishTemplateSheet(Sheet sheet, int columnCount, List<Integer> requestedWidths) {
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(sheet.getLastRowNum(), 1), 0, columnCount - 1));
        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
            int requested = index < requestedWidths.size() ? requestedWidths.get(index) : 24;
            int width = Math.max(sheet.getColumnWidth(index) + 1024, requested * 256);
            sheet.setColumnWidth(index, Math.min(width, 80 * 256));
        }
    }

    public record ExcelRow(int rowNumber, List<String> values) {
        public String value(int index) {
            return values.get(index);
        }
    }

    public record ExcelSheetTemplate(String sheetName, List<String> headers, List<String> sampleValues,
                                     List<Integer> columnWidths) {
        public ExcelSheetTemplate(String sheetName, List<String> headers, List<String> sampleValues) {
            this(sheetName, headers, sampleValues, List.of());
        }
    }
}
