package com.naenae.teacher.wordtest.service;

import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.wordtest.domain.WordTest;
import com.naenae.teacher.wordtest.model.WordTestDetail;
import com.naenae.teacher.wordtest.model.WordTestListItem;
import com.naenae.teacher.wordtest.model.WordTestWordRow;
import com.naenae.teacher.wordtest.repository.WordTestRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherWordTestService {

    private static final int MAX_WORD_COUNT = 100;
    private static final Comparator<CourseOption> COURSE_NAME_ORDER =
            Comparator.comparing(CourseOption::title, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(CourseOption::id);

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final WordTestRepository wordTestRepository;

    public TeacherWordTestService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            WordTestRepository wordTestRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.wordTestRepository = wordTestRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long teacherUserId) {
        Teacher teacher = getTeacher(teacherUserId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .sorted(COURSE_NAME_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getSelectedCourses(Long teacherUserId, List<Long> courseIds) {
        Teacher teacher = getTeacher(teacherUserId);
        return getOwnedCourses(teacher, courseIds).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .sorted(COURSE_NAME_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageView<WordTestListItem> getWordTests(Long teacherUserId, int page) {
        Teacher teacher = getTeacher(teacherUserId);
        return PaginationSupport.toView(
                wordTestRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                        teacher.getId(), PaginationSupport.pageRequest(page)
                ).map(test -> new WordTestListItem(
                        test.getId(),
                        test.getCreatedAt(),
                        test.getStartDate(),
                        test.getEndDate(),
                        test.getCourses().stream()
                                .map(mapping -> mapping.getCourse().getTitle())
                                .sorted()
                                .collect(Collectors.joining(", ")),
                        test.getWords().size()
                ))
        );
    }

    @Transactional(readOnly = true)
    public WordTestDetail getWordTest(Long teacherUserId, Long wordTestId) {
        Teacher teacher = getTeacher(teacherUserId);
        WordTest test = getOwnedWordTest(teacher, wordTestId);
        return new WordTestDetail(
                test.getId(),
                test.getStartDate(),
                test.getEndDate(),
                test.getCourses().stream()
                        .map(mapping -> new CourseOption(mapping.getCourse().getId(), mapping.getCourse().getTitle()))
                        .sorted(COURSE_NAME_ORDER)
                        .toList(),
                test.getWords().stream()
                        .map(word -> new WordTestWordRow(word.getWord(), word.getMeaning()))
                        .toList()
        );
    }

    @Transactional
    public void createWordTest(
            Long teacherUserId,
            List<Long> courseIds,
            LocalDate startDate,
            LocalDate endDate,
            List<String> words,
            List<String> meanings
    ) {
        Teacher teacher = getTeacher(teacherUserId);
        List<Course> courses = getOwnedCourses(teacher, courseIds);
        List<String> normalizedWords = normalizeWords(words);
        validatePeriod(startDate, endDate);

        WordTest wordTest = WordTest.create(teacher, startDate, endDate);
        fillContents(wordTest, courses, normalizedWords, meanings);
        wordTestRepository.save(wordTest);
    }

    @Transactional
    public void updateWordTest(
            Long teacherUserId,
            Long wordTestId,
            List<Long> courseIds,
            LocalDate startDate,
            LocalDate endDate,
            List<String> words,
            List<String> meanings
    ) {
        Teacher teacher = getTeacher(teacherUserId);
        WordTest wordTest = getOwnedWordTest(teacher, wordTestId);
        List<Course> courses = getOwnedCourses(teacher, courseIds);
        List<String> normalizedWords = normalizeWords(words);
        validatePeriod(startDate, endDate);

        wordTest.updatePeriod(startDate, endDate);
        wordTest.clearContents();
        fillContents(wordTest, courses, normalizedWords, meanings);
    }

    @Transactional
    public void deleteWordTest(Long teacherUserId, Long wordTestId) {
        Teacher teacher = getTeacher(teacherUserId);
        wordTestRepository.delete(getOwnedWordTest(teacher, wordTestId));
    }

    private void fillContents(WordTest wordTest, List<Course> courses, List<String> words, List<String> meanings) {
        courses.forEach(wordTest::addCourse);
        for (int index = 0; index < words.size(); index++) {
            String meaning = meanings != null && index < meanings.size() ? trimToNull(meanings.get(index)) : null;
            wordTest.addWord(index + 1, words.get(index), meaning);
        }
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시험 게시 시작일과 종료일을 입력해 주세요.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private List<Course> getOwnedCourses(Teacher teacher, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("단어시험을 등록할 반을 1개 이상 선택해 주세요.");
        }
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(courseIds));
        List<Course> courses = courseRepository.findByTeacherIdAndIdInOrderByTitleAsc(teacher.getId(), distinctIds);
        if (courses.size() != distinctIds.size()) {
            throw new IllegalArgumentException("선택한 반 정보를 확인할 수 없습니다.");
        }
        return courses;
    }

    private List<String> normalizeWords(List<String> words) {
        if (words == null || words.isEmpty() || words.size() > MAX_WORD_COUNT) {
            throw new IllegalArgumentException("단어는 1개 이상 100개 이하로 입력해 주세요.");
        }
        List<String> normalized = words.stream().map(this::trimToNull).toList();
        if (normalized.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException("비어 있는 단어 입력칸이 있습니다.");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private WordTest getOwnedWordTest(Teacher teacher, Long wordTestId) {
        return wordTestRepository.findByIdAndTeacherId(wordTestId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("단어시험을 찾을 수 없습니다."));
    }
}
