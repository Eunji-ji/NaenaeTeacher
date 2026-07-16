package com.naenae.common.vocabulary.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "today_words",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_words_teacher_level_word", columnNames = {"teacher_id", "level", "word"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayWord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @Column(nullable = false, length = 120)
    private String word;

    @Column(name = "meaning_ko", columnDefinition = "TEXT")
    private String meaningKo;

    public static TodayWord create(Teacher teacher, WordLevel level, String word, String meaningKo) {
        TodayWord todayWord = new TodayWord();
        todayWord.teacher = teacher;
        todayWord.level = level;
        todayWord.word = word;
        todayWord.meaningKo = meaningKo;
        return todayWord;
    }

    public Long getId() {
        return id;
    }

    public WordLevel getLevel() {
        return level;
    }

    public String getWord() {
        return word;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public String getMeaningKo() {
        return meaningKo;
    }
}
