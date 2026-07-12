package com.naenae.teacher.assignment.domain;

import com.naenae.teacher.course.domain.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Entity @Table(name = "assignment_courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentCourse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assignment_id", nullable = false) private Assignment assignment;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false) private Course course;
    static AssignmentCourse create(Assignment assignment, Course course) {
        AssignmentCourse value = new AssignmentCourse(); value.assignment = assignment; value.course = course; return value;
    }
}
