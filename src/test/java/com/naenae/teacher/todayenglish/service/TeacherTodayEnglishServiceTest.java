package com.naenae.teacher.todayenglish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.common.excel.ExcelFileService;
import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.repository.TodayWordRepository;
import com.naenae.common.vocabulary.repository.TodayWordSelectionRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class TeacherTodayEnglishServiceTest {
    private TeacherRepository teacherRepository;
    private TodayWordRepository wordRepository;
    private TodayWordSelectionRepository selectionRepository;
    private TeacherTodayEnglishService service;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacherRepository = mock(TeacherRepository.class);
        wordRepository = mock(TodayWordRepository.class);
        selectionRepository = mock(TodayWordSelectionRepository.class);
        teacher = mock(Teacher.class);
        when(teacher.getId()).thenReturn(9L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));
        service = new TeacherTodayEnglishService(teacherRepository, wordRepository, selectionRepository,
                new ExcelFileService());
    }

    @Test
    void replacesWordPoolWhileAllowingDifferentColumnLengths() throws Exception {
        MockMultipartFile file = workbook(false);

        var counts = service.replaceWords(3L, file);

        assertThat(counts.level1()).isEqualTo(2);
        assertThat(counts.level2()).isEqualTo(2);
        assertThat(counts.level3()).isEqualTo(2);
        verify(selectionRepository).deleteByTeacherId(9L);
        verify(wordRepository).deleteByTeacherId(9L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TodayWord>> captor = ArgumentCaptor.forClass(List.class);
        verify(wordRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(TodayWord::getLevel)
                .containsExactlyInAnyOrder(WordLevel.LOWER_ELEMENTARY, WordLevel.LOWER_ELEMENTARY,
                        WordLevel.UPPER_ELEMENTARY, WordLevel.UPPER_ELEMENTARY,
                        WordLevel.MIDDLE_SCHOOL, WordLevel.MIDDLE_SCHOOL);
        assertThat(captor.getValue()).extracting(TodayWord::getMeaningKo)
                .contains("사과", "바나나", "도전", "창의적인", "환경", "과학");
    }

    @Test
    void rejectsUploadWhenARequiredLevelColumnHasNoWords() throws Exception {
        MockMultipartFile file = workbook(true);

        assertThatThrownBy(() -> service.replaceWords(3L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LEVEL2");
        verify(selectionRepository, never()).deleteByTeacherId(9L);
        verify(wordRepository, never()).saveAll(anyList());
    }

    @Test
    void listsOnlyTheSelectedLevelWithinTheTeacherScope() {
        TodayWord word = TodayWord.create(teacher, WordLevel.UPPER_ELEMENTARY, "challenge", "도전");
        when(wordRepository.findByTeacherIdAndLevelOrderByWordAsc(
                eq(9L), eq(WordLevel.UPPER_ELEMENTARY), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(word)));

        var page = service.getWords(3L, WordLevel.UPPER_ELEMENTARY, 0);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().word()).isEqualTo("challenge");
        assertThat(page.content().getFirst().meaning()).isEqualTo("도전");
    }

    @Test
    void deletesOwnedWordAndItsDailySelections() {
        TodayWord word = mock(TodayWord.class);
        when(word.getId()).thenReturn(5L);
        when(wordRepository.findByIdAndTeacherId(5L, 9L)).thenReturn(Optional.of(word));

        service.deleteWord(3L, 5L);

        verify(selectionRepository).deleteByTodayWordId(5L);
        verify(wordRepository).delete(word);
    }

    private MockMultipartFile workbook(boolean emptyLevel2) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            writeSheet(workbook, "LEVEL1", List.of(List.of("apple", "사과"), List.of("banana", "바나나")));
            writeSheet(workbook, "LEVEL2", emptyLevel2 ? List.of() :
                    List.of(List.of("challenge", "도전"), List.of("creative", "창의적인")));
            writeSheet(workbook, "LEVEL3", List.of(List.of("environment", "환경"), List.of("science", "과학")));
            workbook.write(output);
            return new MockMultipartFile("file", "today-english.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }

    private void writeSheet(XSSFWorkbook workbook, String name, List<List<String>> values) {
        var sheet = workbook.createSheet(name);
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("단어");
        header.createCell(1).setCellValue("뜻");
        for (int index = 0; index < values.size(); index++) {
            var row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(values.get(index).get(0));
            row.createCell(1).setCellValue(values.get(index).get(1));
        }
    }
}
