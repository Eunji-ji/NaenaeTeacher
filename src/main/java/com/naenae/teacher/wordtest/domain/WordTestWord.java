package com.naenae.teacher.wordtest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "word_test_words")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordTestWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_test_id", nullable = false)
    private WordTest wordTest;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false, length = 150)
    private String word;

    @Column(length = 300)
    private String meaning;

    static WordTestWord create(WordTest wordTest, int displayOrder, String word, String meaning) {
        WordTestWord testWord = new WordTestWord();
        testWord.wordTest = wordTest;
        testWord.displayOrder = displayOrder;
        testWord.word = word;
        testWord.meaning = meaning;
        return testWord;
    }
}
