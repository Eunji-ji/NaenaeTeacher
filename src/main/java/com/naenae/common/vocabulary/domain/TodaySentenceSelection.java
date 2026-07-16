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
import com.naenae.teacher.profile.domain.Teacher;

@Entity
@Table(
        name = "today_sentence_selections",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_sentence_selections_teacher_date_level", columnNames = {"teacher_id", "selection_date", "level"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodaySentenceSelection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "selection_date", nullable = false)
    private LocalDate selectionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private WordLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_sentence_id", nullable = false)
    private TodaySentence todaySentence;

    public static TodaySentenceSelection create(Teacher teacher, LocalDate selectionDate,
                                                WordLevel level, TodaySentence todaySentence) {
        TodaySentenceSelection selection = new TodaySentenceSelection();
        selection.teacher = teacher;
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

    public Teacher getTeacher() {
        return teacher;
    }

    public WordLevel getLevel() {
        return level;
    }

    public TodaySentence getTodaySentence() {
        return todaySentence;
    }
}
