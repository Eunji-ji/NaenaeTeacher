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
        name = "today_words",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_words_level_word", columnNames = {"level", "word"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayWord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @Column(nullable = false, length = 120)
    private String word;

    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    public static TodayWord create(WordLevel level, String word, String sentence) {
        TodayWord todayWord = new TodayWord();
        todayWord.level = level;
        todayWord.word = word;
        todayWord.sentence = sentence;
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

    public String getSentence() {
        return sentence;
    }
}
