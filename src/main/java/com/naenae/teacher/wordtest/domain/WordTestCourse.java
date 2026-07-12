package com.naenae.teacher.wordtest.domain;

import com.naenae.teacher.course.domain.Course;
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
@Table(name = "word_test_courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordTestCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_test_id", nullable = false)
    private WordTest wordTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    static WordTestCourse create(WordTest wordTest, Course course) {
        WordTestCourse mapping = new WordTestCourse();
        mapping.wordTest = wordTest;
        mapping.course = course;
        return mapping;
    }
}
