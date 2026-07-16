package com.naenae.common.vocabulary.domain;

import com.naenae.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.naenae.teacher.profile.domain.Teacher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "today_sentences",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_sentences_teacher_level_sentence", columnNames = {"teacher_id", "level", "sentence"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodaySentence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    @Column(name = "meaning_ko", columnDefinition = "TEXT")
    private String meaningKo;

    public static TodaySentence create(Teacher teacher, WordLevel level, String sentence, String meaningKo) {
        TodaySentence todaySentence = new TodaySentence();
        todaySentence.teacher = teacher;
        todaySentence.level = level;
        todaySentence.sentence = sentence;
        todaySentence.meaningKo = meaningKo;
        return todaySentence;
    }

    public Long getId() {
        return id;
    }

    public WordLevel getLevel() {
        return level;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public String getSentence() {
        return sentence;
    }

    public String getMeaningKo() {
        return meaningKo;
    }
}
