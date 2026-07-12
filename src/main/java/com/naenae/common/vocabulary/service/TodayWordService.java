package com.naenae.common.vocabulary.service;

import java.time.LocalDate;
import java.util.List;

import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.TodayWordSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodayWordView;
import com.naenae.common.vocabulary.repository.TodayWordRepository;
import com.naenae.common.vocabulary.repository.TodayWordSelectionRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodayWordService {

    private static final String KOREAN_MIDDLE = "\uC911";
    private static final String KOREAN_UPPER = "\uACE0";

    private final TodayWordRepository todayWordRepository;
    private final TodayWordSelectionRepository todayWordSelectionRepository;
    private final CourseStudentRepository courseStudentRepository;

    public TodayWordService(
            TodayWordRepository todayWordRepository,
            TodayWordSelectionRepository todayWordSelectionRepository,
            CourseStudentRepository courseStudentRepository
    ) {
        this.todayWordRepository = todayWordRepository;
        this.todayWordSelectionRepository = todayWordSelectionRepository;
        this.courseStudentRepository = courseStudentRepository;
    }

    @Transactional
    public List<TodayWordView> getTeacherTodayWords(LocalDate date) {
        return List.of(
                getOrCreateSelection(date, WordLevel.LOWER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.UPPER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.MIDDLE_SCHOOL)
        );
    }

    @Transactional
    public TodayWordView getStudentTodayWord(LocalDate date, Student student) {
        WordLevel level = resolveLevel(student);
        return getOrCreateSelection(date, level);
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
                if (parsedGrade <= 3) {
                    return WordLevel.LOWER_ELEMENTARY;
                }
                if (parsedGrade <= 6) {
                    return WordLevel.UPPER_ELEMENTARY;
                }
            }
        }

        List<CourseStudent> courseStudents = courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(student.getId());
        for (CourseStudent mapping : courseStudents) {
            String title = mapping.getCourse().getTitle();
            if (title != null && title.contains(KOREAN_MIDDLE)) {
                return WordLevel.MIDDLE_SCHOOL;
            }
            if (title != null && (title.contains("4") || title.contains("5") || title.contains("6") || title.contains(KOREAN_UPPER))) {
                return WordLevel.UPPER_ELEMENTARY;
            }
        }
        return WordLevel.LOWER_ELEMENTARY;
    }

    private TodayWordView getOrCreateSelection(LocalDate date, WordLevel level) {
        TodayWordSelection selection = todayWordSelectionRepository.findBySelectionDateAndLevel(date, level)
                .orElseGet(() -> todayWordSelectionRepository.save(
                        TodayWordSelection.create(date, level, pickWord(date, level))
                ));
        TodayWord todayWord = selection.getTodayWord();
        return new TodayWordView(
                level,
                level.getLabel(),
                todayWord.getWord(),
                todayWord.getSentence()
        );
    }

    private TodayWord pickWord(LocalDate date, WordLevel level) {
        List<TodayWord> words = todayWordRepository.findByLevelOrderByWordAsc(level);
        validateWordsExist(words);
        int index = Math.floorMod((int) (date.toEpochDay() * 31 + level.ordinal() * 997), words.size());
        return words.get(index);
    }

    private void validateWordsExist(List<TodayWord> words) {
        if (words.isEmpty()) {
            throw new IllegalStateException("Today word data is not ready. Seed today_words first.");
        }
    }
}
