package com.naenae.teacher.todayenglish.service;

import com.naenae.common.excel.ExcelFileService;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.repository.TodayWordRepository;
import com.naenae.common.vocabulary.repository.TodayWordSelectionRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.todayenglish.model.TodayEnglishWordCounts;
import com.naenae.teacher.todayenglish.model.TodayEnglishWordItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherTodayEnglishService {
    private static final List<String> HEADERS = List.of("단어", "뜻");
    private static final List<String> SHEET_NAMES = List.of("LEVEL1", "LEVEL2", "LEVEL3");
    private static final List<WordLevel> LEVELS = List.of(
            WordLevel.LOWER_ELEMENTARY, WordLevel.UPPER_ELEMENTARY, WordLevel.MIDDLE_SCHOOL);

    private final TeacherRepository teacherRepository;
    private final TodayWordRepository todayWordRepository;
    private final TodayWordSelectionRepository todayWordSelectionRepository;
    private final ExcelFileService excelFileService;

    public TeacherTodayEnglishService(TeacherRepository teacherRepository,
                                      TodayWordRepository todayWordRepository,
                                      TodayWordSelectionRepository todayWordSelectionRepository,
                                      ExcelFileService excelFileService) {
        this.teacherRepository = teacherRepository;
        this.todayWordRepository = todayWordRepository;
        this.todayWordSelectionRepository = todayWordSelectionRepository;
        this.excelFileService = excelFileService;
    }

    @Transactional(readOnly = true)
    public byte[] createTemplate(Long teacherUserId) {
        teacher(teacherUserId);
        return excelFileService.createMultiSheetTemplate(List.of(
                new ExcelFileService.ExcelSheetTemplate("LEVEL1", HEADERS, List.of("apple", "사과"), List.of(28, 42)),
                new ExcelFileService.ExcelSheetTemplate("LEVEL2", HEADERS, List.of("challenge", "도전"), List.of(28, 42)),
                new ExcelFileService.ExcelSheetTemplate("LEVEL3", HEADERS, List.of("environment", "환경"), List.of(28, 42))));
    }

    @Transactional(readOnly = true)
    public TodayEnglishWordCounts getCounts(Long teacherUserId) {
        Teacher teacher = teacher(teacherUserId);
        return new TodayEnglishWordCounts(
                todayWordRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.LOWER_ELEMENTARY),
                todayWordRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.UPPER_ELEMENTARY),
                todayWordRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.MIDDLE_SCHOOL));
    }

    @Transactional(readOnly = true)
    public PageView<TodayEnglishWordItem> getWords(Long teacherUserId, WordLevel level, int page) {
        Teacher teacher = teacher(teacherUserId);
        var words = level == null
                ? todayWordRepository.findByTeacherIdOrderByLevelAscWordAsc(
                        teacher.getId(), PaginationSupport.pageRequest(page))
                : todayWordRepository.findByTeacherIdAndLevelOrderByWordAsc(
                        teacher.getId(), level, PaginationSupport.pageRequest(page));
        return PaginationSupport.toView(words.map(this::toItem));
    }

    @Transactional
    public void deleteWord(Long teacherUserId, Long wordId) {
        Teacher teacher = teacher(teacherUserId);
        TodayWord word = todayWordRepository.findByIdAndTeacherId(wordId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 단어를 찾을 수 없습니다."));
        todayWordSelectionRepository.deleteByTodayWordId(word.getId());
        todayWordRepository.delete(word);
    }

    @Transactional
    public TodayEnglishWordCounts replaceWords(Long teacherUserId, MultipartFile file) {
        Teacher teacher = teacher(teacherUserId);
        Map<WordLevel, LinkedHashMap<String, WordEntry>> words = new LinkedHashMap<>();
        LEVELS.forEach(level -> words.put(level, new LinkedHashMap<>()));

        Map<String, List<String>> expectedSheets = new LinkedHashMap<>();
        SHEET_NAMES.forEach(name -> expectedSheets.put(name, HEADERS));
        Map<String, List<ExcelFileService.ExcelRow>> sheets = excelFileService.readSheets(file, expectedSheets);

        for (int index = 0; index < LEVELS.size(); index++) {
            String sheetName = SHEET_NAMES.get(index);
            WordLevel level = LEVELS.get(index);
            for (ExcelFileService.ExcelRow row : sheets.get(sheetName)) {
                String word = row.value(0);
                String meaning = row.value(1);
                if (word == null || meaning == null) {
                    throw new IllegalArgumentException(sheetName + " 시트 " + row.rowNumber() + "행: 단어와 뜻을 모두 입력해 주세요.");
                }
                if (word.length() > 120) {
                    throw new IllegalArgumentException(sheetName + " 시트 " + row.rowNumber() + "행: 단어는 120자 이하여야 합니다.");
                }
                words.get(level).putIfAbsent(word.toLowerCase(Locale.ROOT), new WordEntry(word, meaning));
            }
        }

        for (int index = 0; index < LEVELS.size(); index++) {
            if (words.get(LEVELS.get(index)).isEmpty()) {
                throw new IllegalArgumentException(SHEET_NAMES.get(index) + " 시트에 단어를 한 개 이상 입력해 주세요.");
            }
        }

        todayWordSelectionRepository.deleteByTeacherId(teacher.getId());
        todayWordRepository.deleteByTeacherId(teacher.getId());
        List<TodayWord> entities = new ArrayList<>();
        LEVELS.forEach(level -> words.get(level).values()
                .forEach(entry -> entities.add(TodayWord.create(teacher, level, entry.word(), entry.meaning()))));
        todayWordRepository.saveAll(entities);

        return new TodayEnglishWordCounts(
                words.get(WordLevel.LOWER_ELEMENTARY).size(),
                words.get(WordLevel.UPPER_ELEMENTARY).size(),
                words.get(WordLevel.MIDDLE_SCHOOL).size());
    }

    private Teacher teacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private TodayEnglishWordItem toItem(TodayWord word) {
        return new TodayEnglishWordItem(word.getId(), word.getLevel(), word.getLevel().getLabel(),
                word.getWord(), word.getMeaningKo());
    }

    private record WordEntry(String word, String meaning) {
    }
}
