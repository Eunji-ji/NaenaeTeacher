package com.naenae.common.vocabulary.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.naenae.common.vocabulary.domain.TodayWord;
import com.naenae.common.vocabulary.domain.TodayWordSelection;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.common.vocabulary.model.TodayWordView;
import com.naenae.common.vocabulary.repository.TodayWordRepository;
import com.naenae.common.vocabulary.repository.TodayWordSelectionRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodayWordService {

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

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedIfNeeded() {
        if (todayWordRepository.count() > 0) {
            return;
        }
        todayWordRepository.saveAll(buildSeedWords());
    }

    @Transactional(readOnly = true)
    public List<TodayWordView> getTeacherTodayWords(LocalDate date) {
        return List.of(
                getOrCreateSelection(date, WordLevel.LOWER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.UPPER_ELEMENTARY),
                getOrCreateSelection(date, WordLevel.MIDDLE_SCHOOL)
        );
    }

    @Transactional(readOnly = true)
    public TodayWordView getStudentTodayWord(LocalDate date, Student student) {
        WordLevel level = resolveLevel(student);
        return getOrCreateSelection(date, level);
    }

    @Transactional(readOnly = true)
    public WordLevel resolveLevel(Student student) {
        String grade = student.getGrade();
        if (grade != null && !grade.isBlank()) {
            String normalized = grade.trim();
            if (normalized.contains("중")) {
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
            if (title != null && title.contains("중")) {
                return WordLevel.MIDDLE_SCHOOL;
            }
            if (title != null && (title.contains("4") || title.contains("5") || title.contains("6") || title.contains("고"))) {
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
        if (words.isEmpty()) {
            throw new IllegalStateException("오늘의 단어가 아직 준비되지 않았습니다.");
        }
        int index = Math.floorMod((int) (date.toEpochDay() * 31 + level.ordinal() * 997), words.size());
        return words.get(index);
    }

    private List<TodayWord> buildSeedWords() {
        List<TodayWord> words = new ArrayList<>();
        words.addAll(buildLevelWords(WordLevel.LOWER_ELEMENTARY, lowerRoots(), "I can use %s today."));
        words.addAll(buildLevelWords(WordLevel.UPPER_ELEMENTARY, upperRoots(), "We can learn %s in class."));
        words.addAll(buildLevelWords(WordLevel.MIDDLE_SCHOOL, middleRoots(), "Please remember %s for today."));
        return words;
    }

    private List<TodayWord> buildLevelWords(WordLevel level, List<String> roots, String sentenceTemplate) {
        Set<String> uniqueWords = new LinkedHashSet<>();
        List<WordPattern> patterns = List.of(
                root -> root,
                this::pluralize,
                this::pastTense,
                this::presentParticiple,
                root -> "re" + root,
                root -> "un" + root,
                root -> "dis" + root,
                root -> "over" + root,
                root -> "under" + root,
                root -> "non" + root,
                root -> root + "er",
                root -> root + "est",
                root -> root + "ful",
                root -> root + "less",
                root -> root + "ness",
                root -> root + "ment",
                root -> root + "able",
                root -> root + "ive",
                root -> root + "ity",
                root -> root + "ion",
                root -> root + "ly",
                root -> root + "al",
                root -> root + "ous",
                root -> root + "ship",
                root -> root + "age",
                root -> root + "wise",
                root -> "super" + root,
                root -> "mini" + root,
                root -> "post" + root,
                root -> "pre" + root,
                root -> "micro" + root
        );

        List<TodayWord> words = new ArrayList<>();
        for (String root : roots) {
            for (WordPattern pattern : patterns) {
                String candidate = normalize(pattern.apply(root));
                if (candidate.isBlank() || candidate.length() < 3 || !candidate.chars().allMatch(Character::isLetter)) {
                    continue;
                }
                if (uniqueWords.add(candidate)) {
                    words.add(TodayWord.create(level, candidate, String.format(sentenceTemplate, candidate)));
                }
                if (words.size() == 1000) {
                    return words;
                }
            }
        }

        while (words.size() < 1000) {
            for (String root : roots) {
                String candidate = normalize(root + words.size());
                if (uniqueWords.add(candidate)) {
                    words.add(TodayWord.create(level, candidate, String.format(sentenceTemplate, candidate)));
                }
                if (words.size() == 1000) {
                    return words;
                }
            }
        }

        return words;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String pluralize(String root) {
        if (root.endsWith("s") || root.endsWith("x") || root.endsWith("ch") || root.endsWith("sh")) {
            return root + "es";
        }
        if (root.endsWith("y") && root.length() > 1 && !isVowel(root.charAt(root.length() - 2))) {
            return root.substring(0, root.length() - 1) + "ies";
        }
        return root + "s";
    }

    private String pastTense(String root) {
        if (root.endsWith("e")) {
            return root + "d";
        }
        if (root.endsWith("y") && root.length() > 1 && !isVowel(root.charAt(root.length() - 2))) {
            return root.substring(0, root.length() - 1) + "ied";
        }
        return root + "ed";
    }

    private String presentParticiple(String root) {
        if (root.endsWith("e") && !root.endsWith("ee")) {
            return root.substring(0, root.length() - 1) + "ing";
        }
        return root + "ing";
    }

    private boolean isVowel(char character) {
        return "aeiou".indexOf(Character.toLowerCase(character)) >= 0;
    }

    private List<String> lowerRoots() {
        return List.of(
                "apple", "ball", "book", "boy", "bread", "car", "cat", "chair", "clean", "color",
                "day", "dog", "eat", "family", "friend", "game", "girl", "good", "happy", "home",
                "house", "jump", "kind", "learn", "like", "love", "milk", "music", "name", "open",
                "play", "read", "run", "school", "small", "star", "sun", "water", "white", "write"
        );
    }

    private List<String> upperRoots() {
        return List.of(
                "about", "answer", "before", "believe", "birthday", "careful", "choose", "decide", "discover", "enough",
                "example", "famous", "forget", "future", "gather", "helpful", "idea", "journey", "language", "message",
                "nature", "opinion", "provide", "regular", "schedule", "search", "special", "support", "teacher", "travel",
                "understand", "useful", "village", "weather", "welcome", "whisper", "wonder", "already", "continue", "direction"
        );
    }

    private List<String> middleRoots() {
        return List.of(
                "ability", "achieve", "adult", "affect", "against", "behavior", "communicate", "compete", "confident", "difference",
                "education", "environment", "frequency", "generation", "independent", "influence", "internet", "knowledge", "laboratory", "medicine",
                "population", "pressure", "relationship", "responsibility", "solution", "strategy", "technology", "tradition", "universe", "valuable",
                "variety", "weakness", "benefit", "challenge", "conclusion", "experiment", "global", "harmony", "identity", "perspective"
        );
    }

    @FunctionalInterface
    private interface WordPattern {
        String apply(String root);
    }
}
