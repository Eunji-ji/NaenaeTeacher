package com.naenae.common.excel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.WorkbookFactory;

class ExcelFileServiceTest {

    private final ExcelFileService excelFileService = new ExcelFileService();

    @Test
    void createsTemplateAndReadsRowsWithSharedHeaderValidation() {
        List<String> headers = List.of("이름", "반");
        byte[] template = excelFileService.createTemplate("학생", headers, List.of("홍길동", "A반"));
        var upload = new MockMultipartFile(
                "file",
                "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                template
        );

        List<ExcelFileService.ExcelRow> rows = excelFileService.readRows(upload, headers);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().rowNumber()).isEqualTo(2);
        assertThat(rows.getFirst().values()).containsExactly("홍길동", "A반");
    }

    @Test
    void createsAndReadsMultipleNamedSheets() {
        List<String> headers = List.of("단어", "뜻");
        byte[] template = excelFileService.createMultiSheetTemplate(List.of(
                new ExcelFileService.ExcelSheetTemplate("LEVEL1", headers, List.of("apple", "사과")),
                new ExcelFileService.ExcelSheetTemplate("LEVEL2", headers, List.of("challenge", "도전")),
                new ExcelFileService.ExcelSheetTemplate("LEVEL3", headers, List.of("environment", "환경"))));
        var upload = new MockMultipartFile("file", "words.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", template);
        var expected = new LinkedHashMap<String, List<String>>();
        expected.put("LEVEL1", headers);
        expected.put("LEVEL2", headers);
        expected.put("LEVEL3", headers);

        var sheets = excelFileService.readSheets(upload, expected);

        assertThat(sheets.keySet()).containsExactly("LEVEL1", "LEVEL2", "LEVEL3");
        assertThat(sheets.get("LEVEL1").getFirst().values()).containsExactly("apple", "사과");
        assertThat(sheets.get("LEVEL3").getFirst().values()).containsExactly("environment", "환경");
    }

    @Test
    void stylesHeadersAndUsesRequestedWideColumns() throws Exception {
        byte[] template = excelFileService.createMultiSheetTemplate(List.of(
                new ExcelFileService.ExcelSheetTemplate("LEVEL1", List.of("문장", "뜻"),
                        List.of("I like books.", "나는 책을 좋아해요."), List.of(72, 55))));

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(template))) {
            var sheet = workbook.getSheet("LEVEL1");
            var header = sheet.getRow(0);
            assertThat(header.getHeightInPoints()).isGreaterThanOrEqualTo(26);
            assertThat(header.getCell(0).getCellStyle().getFillForegroundColor())
                    .isEqualTo(IndexedColors.TEAL.getIndex());
            assertThat(sheet.getColumnWidth(0)).isGreaterThanOrEqualTo(72 * 256);
            assertThat(sheet.getColumnWidth(1)).isGreaterThanOrEqualTo(55 * 256);
            assertThat(sheet.getPaneInformation()).isNotNull();
        }
    }
}
