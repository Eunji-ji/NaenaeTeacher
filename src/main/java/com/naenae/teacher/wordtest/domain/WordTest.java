package com.naenae.teacher.wordtest.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "word_tests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "wordTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<WordTestCourse> courses = new ArrayList<>();

    @OrderBy("displayOrder ASC")
    @OneToMany(mappedBy = "wordTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<WordTestWord> words = new ArrayList<>();

    public static WordTest create(Teacher teacher, LocalDate startDate, LocalDate endDate) {
        WordTest wordTest = new WordTest();
        wordTest.teacher = teacher;
        wordTest.startDate = startDate;
        wordTest.endDate = endDate;
        return wordTest;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void clearContents() {
        courses.clear();
        words.clear();
    }

    public void addCourse(Course course) {
        courses.add(WordTestCourse.create(this, course));
    }

    public void addWord(int displayOrder, String word, String meaning) {
        words.add(WordTestWord.create(this, displayOrder, word, meaning));
    }
}
