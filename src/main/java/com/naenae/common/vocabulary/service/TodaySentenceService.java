package com.naenae.common.vocabulary.service;

import java.time.LocalDate;
import java.util.List;

import com.naenae.common.vocabulary.domain.TodaySentence;
import com.naenae.common.vocabulary.domain.TodaySentenceSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodaySentenceView;
import com.naenae.common.vocabulary.repository.TodaySentenceRepository;
import com.naenae.common.vocabulary.repository.TodaySentenceSelectionRepository;
import com.naenae.student.profile.domain.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodaySentenceService {

    private final TodaySentenceRepository todaySentenceRepository;
    private final TodaySentenceSelectionRepository todaySentenceSelectionRepository;
    private final TodayWordService todayWordService;

    public TodaySentenceService(
            TodaySentenceRepository todaySentenceRepository,
            TodaySentenceSelectionRepository todaySentenceSelectionRepository,
            TodayWordService todayWordService
    ) {
        this.todaySentenceRepository = todaySentenceRepository;
        this.todaySentenceSelectionRepository = todaySentenceSelectionRepository;
        this.todayWordService = todayWordService;
    }

    @Transactional
    public List<TodaySentenceView> getTeacherTodaySentences(LocalDate date) {
        return List.of(
                getOrCreateSelection(date, WordLevel.LOWER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.UPPER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.MIDDLE_SCHOOL)
        );
    }

    @Transactional
    public TodaySentenceView getStudentTodaySentence(LocalDate date, Student student) {
        WordLevel level = todayWordService.resolveLevel(student);
        return getOrCreateSelection(date, level);
    }

    private TodaySentenceView getOrCreateSelection(LocalDate date, WordLevel level) {
        TodaySentenceSelection selection = todaySentenceSelectionRepository.findBySelectionDateAndLevel(date, level)
                .orElseGet(() -> todaySentenceSelectionRepository.save(
                        TodaySentenceSelection.create(date, level, pickSentence(date, level))
                ));
        TodaySentence todaySentence = selection.getTodaySentence();
        return new TodaySentenceView(
                level,
                level.getLabel(),
                todaySentence.getSentence()
        );
    }

    private TodaySentence pickSentence(LocalDate date, WordLevel level) {
        List<TodaySentence> sentences = todaySentenceRepository.findByLevelOrderBySentenceAsc(level);
        validateSentencesExist(sentences);
        int index = Math.floorMod((int) (date.toEpochDay() * 37 + level.ordinal() * 991), sentences.size());
        return sentences.get(index);
    }

    private void validateSentencesExist(List<TodaySentence> sentences) {
        if (sentences.isEmpty()) {
            throw new IllegalStateException("Today sentence data is not ready. Seed today_sentences first.");
        }
    }
}
