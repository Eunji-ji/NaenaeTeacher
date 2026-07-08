package com.naenae.student.profile.domain;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "student_academic_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_academic_record_year_type", columnNames = {"student_id", "exam_year", "exam_type"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentAcademicRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "exam_year", nullable = false)
    private Integer examYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 20)
    private AcademicExamType examType;

    @Column(nullable = false)
    private Integer score;

    public static StudentAcademicRecord create(Teacher teacher, Student student, Integer examYear, AcademicExamType examType, Integer score) {
        StudentAcademicRecord record = new StudentAcademicRecord();
        record.teacher = teacher;
        record.student = student;
        record.examYear = examYear;
        record.examType = examType;
        record.score = score;
        return record;
    }

    public void updateScore(Integer score) {
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Student getStudent() {
        return student;
    }

    public Integer getExamYear() {
        return examYear;
    }

    public AcademicExamType getExamType() {
        return examType;
    }

    public Integer getScore() {
        return score;
    }
}
