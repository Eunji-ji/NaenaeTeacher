package com.naenae.common.vocabulary.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.TodaySentenceSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodaySentenceView;
import com.naenae.common.vocabulary.repository.TodaySentenceRepository;
import com.naenae.common.vocabulary.repository.TodaySentenceSelectionRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodaySentenceService {

    private final TodaySentenceRepository todaySentenceRepository;
    private final TodaySentenceSelectionRepository todaySentenceSelectionRepository;
    private final TodayWordService todayWordService;
    private final TeacherRepository teacherRepository;

    public TodaySentenceService(
            TodaySentenceRepository todaySentenceRepository,
            TodaySentenceSelectionRepository todaySentenceSelectionRepository,
            TodayWordService todayWordService,
            TeacherRepository teacherRepository
    ) {
        this.todaySentenceRepository = todaySentenceRepository;
        this.todaySentenceSelectionRepository = todaySentenceSelectionRepository;
        this.todayWordService = todayWordService;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public List<TodaySentenceView> getTeacherTodaySentences(LocalDate date, Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
        return Arrays.stream(WordLevel.values())
                .map(level -> getOrCreateSelection(teacher, date, level))
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional
    public TodaySentenceView getStudentTodaySentence(LocalDate date, Student student) {
        WordLevel level = todayWordService.resolveLevel(student);
        return getOrCreateSelection(student.getTeacher(), date, level)
                .orElseGet(() -> new TodaySentenceView(level, level.getLabel(),
                        "등록된 문장이 없어요.", "오늘의 영어에서 문장을 등록해 주세요."));
    }

    private Optional<TodaySentenceView> getOrCreateSelection(Teacher teacher, LocalDate date, WordLevel level) {
        Optional<TodaySentenceSelection> existing = todaySentenceSelectionRepository
                .findByTeacherIdAndSelectionDateAndLevel(teacher.getId(), date, level);
        if (existing.isPresent()) return Optional.of(toView(existing.get().getTodaySentence()));
        List<TodaySentence> sentences = todaySentenceRepository
                .findByTeacherIdAndLevelOrderBySentenceAsc(teacher.getId(), level);
        if (sentences.isEmpty()) return Optional.empty();
        TodaySentence selected = sentences.get(ThreadLocalRandom.current().nextInt(sentences.size()));
        TodaySentenceSelection selection = todaySentenceSelectionRepository.save(
                TodaySentenceSelection.create(teacher, date, level, selected));
        return Optional.of(toView(selection.getTodaySentence()));
    }

    private TodaySentenceView toView(TodaySentence todaySentence) {
        return new TodaySentenceView(
                todaySentence.getLevel(),
                todaySentence.getLevel().getLabel(),
                todaySentence.getSentence(),
                todaySentence.getMeaningKo()
        );
    }
}
