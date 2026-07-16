package com.naenae.common.vocabulary.domain;

import java.time.LocalDate;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.profile.domain.Teacher;
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
        name = "today_word_selections",
        uniqueConstraints = @UniqueConstraint(name = "uk_today_word_selections_teacher_date_level", columnNames = {"teacher_id", "selection_date", "level"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayWordSelection extends BaseTimeEntity {

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
    @JoinColumn(name = "today_word_id", nullable = false)
    private TodayWord todayWord;

    public static TodayWordSelection create(Teacher teacher, LocalDate selectionDate, WordLevel level, TodayWord todayWord) {
        TodayWordSelection selection = new TodayWordSelection();
        selection.teacher = teacher;
        selection.selectionDate = selectionDate;
        selection.level = level;
        selection.todayWord = todayWord;
        return selection;
    }

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public LocalDate getSelectionDate() {
        return selectionDate;
    }

    public WordLevel getLevel() {
        return level;
    }

    public TodayWord getTodayWord() {
        return todayWord;
    }
}
