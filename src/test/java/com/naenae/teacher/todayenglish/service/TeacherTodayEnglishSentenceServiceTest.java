package com.naenae.teacher.todayenglish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.common.excel.ExcelFileService;
import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.repository.TodaySentenceRepository;
import com.naenae.common.vocabulary.repository.TodaySentenceSelectionRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

class TeacherTodayEnglishSentenceServiceTest {
    private TeacherRepository teacherRepository;
    private TodaySentenceRepository sentenceRepository;
    private TodaySentenceSelectionRepository selectionRepository;
    private TeacherTodayEnglishSentenceService service;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacherRepository = mock(TeacherRepository.class);
        sentenceRepository = mock(TodaySentenceRepository.class);
        selectionRepository = mock(TodaySentenceSelectionRepository.class);
        teacher = mock(Teacher.class);
        when(teacher.getId()).thenReturn(9L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));
        service = new TeacherTodayEnglishSentenceService(
                teacherRepository, sentenceRepository, selectionRepository, new ExcelFileService());
    }

    @Test
    void replacesTeacherSentencePoolFromLevelSheets() throws Exception {
        var counts = service.replaceSentences(3L, workbook());

        assertThat(counts.level1()).isEqualTo(1);
        assertThat(counts.level2()).isEqualTo(1);
        assertThat(counts.level3()).isEqualTo(1);
        verify(selectionRepository).deleteByTeacherId(9L);
        verify(sentenceRepository).deleteByTeacherId(9L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TodaySentence>> captor = ArgumentCaptor.forClass(List.class);
        verify(sentenceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(TodaySentence::getSentence)
                .containsExactlyInAnyOrder("Teacher uploaded level 1 sentence.",
                        "Teacher uploaded level 2 sentence.", "Teacher uploaded level 3 sentence.");
        assertThat(captor.getValue()).extracting(TodaySentence::getMeaningKo)
                .containsExactlyInAnyOrder("업로드 테스트 뜻 1", "업로드 테스트 뜻 2", "업로드 테스트 뜻 3");
    }

    @Test
    void listsSelectedLevelWithinTeacherScope() {
        TodaySentence sentence = TodaySentence.create(
                teacher, WordLevel.MIDDLE_SCHOOL, "Teacher uploaded level 3 sentence.", "업로드 테스트 뜻 3");
        when(sentenceRepository.findByTeacherIdAndLevelOrderBySentenceAsc(
                eq(9L), eq(WordLevel.MIDDLE_SCHOOL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sentence)));

        var page = service.getSentences(3L, WordLevel.MIDDLE_SCHOOL, 0);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().sentence()).isEqualTo("Teacher uploaded level 3 sentence.");
        assertThat(page.content().getFirst().meaning()).isEqualTo("업로드 테스트 뜻 3");
    }

    @Test
    void deletesSentenceAndItsDailySelections() {
        TodaySentence sentence = mock(TodaySentence.class);
        when(sentence.getId()).thenReturn(7L);
        when(sentenceRepository.findByIdAndTeacherId(7L, 9L)).thenReturn(Optional.of(sentence));

        service.deleteSentence(3L, 7L);

        verify(selectionRepository).deleteByTodaySentenceId(7L);
        verify(sentenceRepository).delete(sentence);
    }

    private MockMultipartFile workbook() throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            writeSheet(workbook, "LEVEL1", "Teacher uploaded level 1 sentence.", "업로드 테스트 뜻 1");
            writeSheet(workbook, "LEVEL2", "Teacher uploaded level 2 sentence.", "업로드 테스트 뜻 2");
            writeSheet(workbook, "LEVEL3", "Teacher uploaded level 3 sentence.", "업로드 테스트 뜻 3");
            workbook.write(output);
            return new MockMultipartFile("file", "sentences.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }

    private void writeSheet(XSSFWorkbook workbook, String name, String sentence, String meaning) {
        var sheet = workbook.createSheet(name);
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("문장");
        header.createCell(1).setCellValue("뜻");
        var row = sheet.createRow(1);
        row.createCell(0).setCellValue(sentence);
        row.createCell(1).setCellValue(meaning);
    }
}
