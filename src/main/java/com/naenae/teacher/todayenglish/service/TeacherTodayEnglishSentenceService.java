package com.naenae.teacher.todayenglish.service;

import com.naenae.common.excel.ExcelFileService;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.repository.TodaySentenceRepository;
import com.naenae.common.vocabulary.repository.TodaySentenceSelectionRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.todayenglish.model.TodayEnglishSentenceCounts;
import com.naenae.teacher.todayenglish.model.TodayEnglishSentenceItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherTodayEnglishSentenceService {
    private static final List<String> HEADERS = List.of("문장", "뜻");
    private static final List<String> SHEET_NAMES = List.of("LEVEL1", "LEVEL2", "LEVEL3");
    private static final List<WordLevel> LEVELS = List.of(
            WordLevel.LOWER_ELEMENTARY, WordLevel.UPPER_ELEMENTARY, WordLevel.MIDDLE_SCHOOL);

    private final TeacherRepository teacherRepository;
    private final TodaySentenceRepository sentenceRepository;
    private final TodaySentenceSelectionRepository selectionRepository;
    private final ExcelFileService excelFileService;

    public TeacherTodayEnglishSentenceService(TeacherRepository teacherRepository,
                                              TodaySentenceRepository sentenceRepository,
                                              TodaySentenceSelectionRepository selectionRepository,
                                              ExcelFileService excelFileService) {
        this.teacherRepository = teacherRepository;
        this.sentenceRepository = sentenceRepository;
        this.selectionRepository = selectionRepository;
        this.excelFileService = excelFileService;
    }

    @Transactional(readOnly = true)
    public byte[] createTemplate(Long teacherUserId) {
        teacher(teacherUserId);
        return excelFileService.createMultiSheetTemplate(List.of(
                new ExcelFileService.ExcelSheetTemplate("LEVEL1", HEADERS,
                        List.of("I like reading books.", "나는 책 읽는 것을 좋아해요."), List.of(72, 55)),
                new ExcelFileService.ExcelSheetTemplate("LEVEL2", HEADERS,
                        List.of("Practice makes progress.", "연습은 발전을 만들어요."), List.of(72, 55)),
                new ExcelFileService.ExcelSheetTemplate("LEVEL3", HEADERS,
                        List.of("Small efforts create meaningful change.", "작은 노력이 의미 있는 변화를 만들어요."), List.of(72, 55))));
    }

    @Transactional(readOnly = true)
    public TodayEnglishSentenceCounts getCounts(Long teacherUserId) {
        Teacher teacher = teacher(teacherUserId);
        return new TodayEnglishSentenceCounts(
                sentenceRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.LOWER_ELEMENTARY),
                sentenceRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.UPPER_ELEMENTARY),
                sentenceRepository.countByTeacherIdAndLevel(teacher.getId(), WordLevel.MIDDLE_SCHOOL));
    }

    @Transactional(readOnly = true)
    public PageView<TodayEnglishSentenceItem> getSentences(Long teacherUserId, WordLevel level, int page) {
        Teacher teacher = teacher(teacherUserId);
        var sentences = level == null
                ? sentenceRepository.findByTeacherIdOrderByLevelAscSentenceAsc(
                        teacher.getId(), PaginationSupport.pageRequest(page))
                : sentenceRepository.findByTeacherIdAndLevelOrderBySentenceAsc(
                        teacher.getId(), level, PaginationSupport.pageRequest(page));
        return PaginationSupport.toView(sentences.map(this::toItem));
    }

    @Transactional
    public TodayEnglishSentenceCounts replaceSentences(Long teacherUserId, MultipartFile file) {
        Teacher teacher = teacher(teacherUserId);
        Map<WordLevel, LinkedHashMap<String, SentenceEntry>> sentences = new LinkedHashMap<>();
        LEVELS.forEach(level -> sentences.put(level, new LinkedHashMap<>()));
        Map<String, List<String>> expectedSheets = new LinkedHashMap<>();
        SHEET_NAMES.forEach(name -> expectedSheets.put(name, HEADERS));
        Map<String, List<ExcelFileService.ExcelRow>> sheets = excelFileService.readSheets(file, expectedSheets);

        for (int index = 0; index < LEVELS.size(); index++) {
            String sheetName = SHEET_NAMES.get(index);
            WordLevel level = LEVELS.get(index);
            for (ExcelFileService.ExcelRow row : sheets.get(sheetName)) {
                String sentence = row.value(0);
                String meaning = row.value(1);
                if (sentence == null || meaning == null) {
                    throw new IllegalArgumentException(sheetName + " 시트 " + row.rowNumber() + "행: 문장과 뜻을 모두 입력해 주세요.");
                }
                if (sentence.length() > 500) {
                    throw new IllegalArgumentException(sheetName + " 시트 " + row.rowNumber() + "행: 문장은 500자 이하여야 합니다.");
                }
                if (meaning.length() > 1000) {
                    throw new IllegalArgumentException(sheetName + " 시트 " + row.rowNumber() + "행: 뜻은 1,000자 이하여야 합니다.");
                }
                sentences.get(level).putIfAbsent(sentence.toLowerCase(Locale.ROOT),
                        new SentenceEntry(sentence, meaning));
            }
        }

        for (int index = 0; index < LEVELS.size(); index++) {
            if (sentences.get(LEVELS.get(index)).isEmpty()) {
                throw new IllegalArgumentException(SHEET_NAMES.get(index) + " 시트에 문장을 한 개 이상 입력해 주세요.");
            }
        }

        selectionRepository.deleteByTeacherId(teacher.getId());
        sentenceRepository.deleteByTeacherId(teacher.getId());
        List<TodaySentence> entities = new ArrayList<>();
        LEVELS.forEach(level -> sentences.get(level).values().forEach(entry -> entities.add(
                TodaySentence.create(teacher, level, entry.sentence(), entry.meaning()))));
        sentenceRepository.saveAll(entities);
        return new TodayEnglishSentenceCounts(
                sentences.get(WordLevel.LOWER_ELEMENTARY).size(),
                sentences.get(WordLevel.UPPER_ELEMENTARY).size(),
                sentences.get(WordLevel.MIDDLE_SCHOOL).size());
    }

    @Transactional
    public void deleteSentence(Long teacherUserId, Long sentenceId) {
        Teacher teacher = teacher(teacherUserId);
        TodaySentence sentence = sentenceRepository.findByIdAndTeacherId(sentenceId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 문장을 찾을 수 없습니다."));
        selectionRepository.deleteByTodaySentenceId(sentence.getId());
        sentenceRepository.delete(sentence);
    }

    private Teacher teacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private TodayEnglishSentenceItem toItem(TodaySentence sentence) {
        return new TodayEnglishSentenceItem(sentence.getId(), sentence.getLevel(), sentence.getLevel().getLabel(),
                sentence.getSentence(), sentence.getMeaningKo());
    }

    private record SentenceEntry(String sentence, String meaning) {
    }
}
