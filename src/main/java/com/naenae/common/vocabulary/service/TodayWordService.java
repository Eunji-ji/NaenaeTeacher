package com.naenae.common.vocabulary.service;

import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.TodayWordSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodayWordView;
import com.naenae.common.vocabulary.repository.TodayWordRepository;
import com.naenae.common.vocabulary.repository.TodayWordSelectionRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodayWordService {
    private static final String KOREAN_MIDDLE = "\uC911";
    private static final String KOREAN_UPPER = "\uACE0";

    private final TodayWordRepository todayWordRepository;
    private final TodayWordSelectionRepository todayWordSelectionRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final TeacherRepository teacherRepository;

    public TodayWordService(TodayWordRepository todayWordRepository,
                            TodayWordSelectionRepository todayWordSelectionRepository,
                            CourseStudentRepository courseStudentRepository,
                            TeacherRepository teacherRepository) {
        this.todayWordRepository = todayWordRepository;
        this.todayWordSelectionRepository = todayWordSelectionRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public List<TodayWordView> getTeacherTodayWords(LocalDate date, Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
        return Arrays.stream(WordLevel.values())
                .map(level -> getOrCreateSelection(teacher, date, level))
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional
    public TodayWordView getStudentTodayWord(LocalDate date, Student student) {
        WordLevel level = resolveLevel(student);
        return getOrCreateSelection(student.getTeacher(), date, level)
                .orElseGet(() -> new TodayWordView(level, level.getLabel(), "등록된 단어가 없어요", "오늘의 영어에서 단어를 등록해 주세요."));
    }

    @Transactional(readOnly = true)
    public WordLevel resolveLevel(Student student) {
        String grade = student.getGrade();
        if (grade != null && !grade.isBlank()) {
            String normalized = grade.trim();
            if (normalized.contains(KOREAN_MIDDLE)) {
                return WordLevel.MIDDLE_SCHOOL;
            }
            String digits = normalized.replaceAll("[^0-9]", "");
            if (!digits.isBlank()) {
                int parsedGrade = Character.digit(digits.charAt(0), 10);
                if (parsedGrade <= 3) return WordLevel.LOWER_ELEMENTARY;
                if (parsedGrade <= 6) return WordLevel.UPPER_ELEMENTARY;
            }
        }

        List<CourseStudent> courseStudents = courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(student.getId());
        for (CourseStudent mapping : courseStudents) {
            String title = mapping.getCourse().getTitle();
            if (title != null && title.contains(KOREAN_MIDDLE)) return WordLevel.MIDDLE_SCHOOL;
            if (title != null && (title.contains("4") || title.contains("5") || title.contains("6") || title.contains(KOREAN_UPPER))) {
                return WordLevel.UPPER_ELEMENTARY;
            }
        }
        return WordLevel.LOWER_ELEMENTARY;
    }

    private Optional<TodayWordView> getOrCreateSelection(Teacher teacher, LocalDate date, WordLevel level) {
        Optional<TodayWordSelection> existing = todayWordSelectionRepository
                .findByTeacherIdAndSelectionDateAndLevel(teacher.getId(), date, level);
        if (existing.isPresent()) return Optional.of(toView(existing.get().getTodayWord()));

        List<TodayWord> words = todayWordRepository.findByTeacherIdAndLevelOrderByWordAsc(teacher.getId(), level);
        if (words.isEmpty()) return Optional.empty();
        TodayWord selected = words.get(ThreadLocalRandom.current().nextInt(words.size()));
        TodayWordSelection selection = todayWordSelectionRepository.save(
                TodayWordSelection.create(teacher, date, level, selected));
        return Optional.of(toView(selection.getTodayWord()));
    }

    private TodayWordView toView(TodayWord todayWord) {
        return new TodayWordView(todayWord.getLevel(), todayWord.getLevel().getLabel(),
                todayWord.getWord(), todayWord.getMeaningKo());
    }
}
