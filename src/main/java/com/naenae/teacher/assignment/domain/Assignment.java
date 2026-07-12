package com.naenae.teacher.assignment.domain;

import java.time.LocalDate;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.OPEN;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AssignmentCourse> courses = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AssignmentAttachment> attachments = new ArrayList<>();

    public static Assignment create(Teacher teacher, String title, String contentHtml, LocalDate startDate, LocalDate endDate) {
        Assignment assignment = new Assignment();
        assignment.teacher = teacher;
        assignment.title = title;
        assignment.description = "";
        assignment.contentHtml = contentHtml;
        assignment.startDate = startDate;
        assignment.endDate = endDate;
        assignment.status = AssignmentStatus.OPEN;
        return assignment;
    }

    public void addCourse(Course course) { courses.add(AssignmentCourse.create(this, course)); }

    public void addAttachment(String originalName, String storedName, String contentType, long fileSize) {
        attachments.add(AssignmentAttachment.create(this, originalName, storedName, contentType, fileSize));
    }
}
