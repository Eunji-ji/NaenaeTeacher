package com.naenae.common.notice.domain;

import com.naenae.teacher.course.domain.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notice_courses", uniqueConstraints = @UniqueConstraint(name = "uk_notice_courses_notice_course", columnNames = {"notice_id", "course_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeCourse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notice_id", nullable = false) private Notice notice;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false) private Course course;
    static NoticeCourse create(Notice notice, Course course) {
        NoticeCourse value = new NoticeCourse(); value.notice = notice; value.course = course; return value;
    }
}