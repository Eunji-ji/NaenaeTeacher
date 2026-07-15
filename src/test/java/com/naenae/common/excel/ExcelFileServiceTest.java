package com.naenae.common.excel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

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
}