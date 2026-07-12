package com.naenae.common.vocabulary.domain;

import java.time.LocalDate;

import com.naenae.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "today_sentence_selections",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_sentence_selections_date_level", columnNames = {"selection_date", "level"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodaySentenceSelection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "selection_date", nullable = false)
    private LocalDate selectionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_sentence_id", nullable = false)
    private TodaySentence todaySentence;

    public static TodaySentenceSelection create(LocalDate selectionDate, WordLevel level, TodaySentence todaySentence) {
        TodaySentenceSelection selection = new TodaySentenceSelection();
        selection.selectionDate = selectionDate;
        selection.level = level;
        selection.todaySentence = todaySentence;
        return selection;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSelectionDate() {
        return selectionDate;
    }

    public WordLevel getLevel() {
        return level;
    }

    public TodaySentence getTodaySentence() {
        return todaySentence;
    }
}
