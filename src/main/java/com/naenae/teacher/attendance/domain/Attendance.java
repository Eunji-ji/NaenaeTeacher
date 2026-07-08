package com.naenae.teacher.attendance.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.course.domain.Course;
import com.naenae.student.profile.domain.Student;
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
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_student_date_course", columnNames = {"student_id", "attendance_date", "course_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_status", length = 20)
    private StudyStatus studyStatus;

    @Column(columnDefinition = "TEXT")
    private String note;

    public static Attendance create(Teacher teacher, Course course, Student student, LocalDate attendanceDate, AttendanceStatus status, LocalDateTime checkedAt) {
        Attendance attendance = new Attendance();
        attendance.teacher = teacher;
        attendance.course = course;
        attendance.student = student;
        attendance.attendanceDate = attendanceDate;
        attendance.checkedAt = checkedAt;
        attendance.status = status;
        return attendance;
    }

    public void updateStatus(AttendanceStatus status, LocalDateTime checkedAt) {
        this.status = status;
        this.checkedAt = checkedAt;
    }

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Course getCourse() {
        return course;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}
