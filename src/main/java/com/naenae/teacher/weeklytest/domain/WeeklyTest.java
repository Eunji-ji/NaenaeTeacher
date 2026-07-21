package com.naenae.teacher.weeklytest.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.student.profile.domain.Student;
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
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "weekly_tests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OrderBy("id ASC")
    @OneToMany(mappedBy = "weeklyTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<WeeklyTestAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "weeklyTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<WeeklyTestScore> scores = new ArrayList<>();

    public static WeeklyTest create(Teacher teacher, Course course, String name, String remarks) {
        WeeklyTest test = new WeeklyTest();
        test.teacher = teacher;
        test.course = course;
        test.name = name;
        test.remarks = remarks;
        return test;
    }

    public void addAttachment(String originalName, String storedName, String contentType, long fileSize) {
        attachments.add(WeeklyTestAttachment.create(this, originalName, storedName, contentType, fileSize));
    }

    public void addStudent(Student student) {
        scores.add(WeeklyTestScore.create(this, student));
    }
}
