package com.naenae.teacher.weeklytest.domain;

import com.naenae.student.profile.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "weekly_test_scores", uniqueConstraints =
        @UniqueConstraint(name = "uk_weekly_test_scores_test_student", columnNames = {"weekly_test_id", "student_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyTestScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "weekly_test_id", nullable = false) private WeeklyTest weeklyTest;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @Column private Integer score;

    static WeeklyTestScore create(WeeklyTest test, Student student) {
        WeeklyTestScore value = new WeeklyTestScore();
        value.weeklyTest = test;
        value.student = student;
        return value;
    }

    public void updateScore(Integer score) {
        this.score = score;
    }
}
