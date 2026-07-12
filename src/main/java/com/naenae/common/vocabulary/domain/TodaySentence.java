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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "today_sentences",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_sentences_level_sentence", columnNames = {"level", "sentence"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodaySentence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    @Column(name = "meaning_ko", columnDefinition = "TEXT")
    private String meaningKo;

    public static TodaySentence create(WordLevel level, String sentence) {
        TodaySentence todaySentence = new TodaySentence();
        todaySentence.level = level;
        todaySentence.sentence = sentence;
        return todaySentence;
    }

    public Long getId() {
        return id;
    }

    public WordLevel getLevel() {
        return level;
    }

    public String getSentence() {
        return sentence;
    }

    public String getMeaningKo() {
        return meaningKo;
    }
}
