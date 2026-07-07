package com.naenae.teacher.assignment.domain;

import java.time.LocalDateTime;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.student.profile.domain.Student;
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
@Table(name = "student_assignment_status", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_assignment_status", columnNames = {"assignment_id", "student_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentAssignmentStatus extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentAssignmentProgressStatus status = StudentAssignmentProgressStatus.NOT_STARTED;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;
}
