package com.naenae.common.notice.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id", nullable = false) private Teacher teacher;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT") private String contentHtml;
    @Column(name = "target_all", nullable = false) private boolean targetAll;
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<NoticeCourse> courses = new ArrayList<>();
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<NoticeAttachment> attachments = new ArrayList<>();

    public static Notice create(Teacher teacher, String title, String contentHtml, boolean targetAll) {
        Notice notice = new Notice(); notice.teacher = teacher; notice.title = title;
        notice.contentHtml = contentHtml; notice.targetAll = targetAll; return notice;
    }
    public void update(String title, String contentHtml, boolean targetAll) {
        this.title = title; this.contentHtml = contentHtml; this.targetAll = targetAll;
    }
    public void replaceCourses(List<Course> selectedCourses) {
        Set<Long> selectedIds = selectedCourses.stream().map(Course::getId).collect(java.util.stream.Collectors.toSet());
        courses.removeIf(mapping -> !selectedIds.contains(mapping.getCourse().getId()));
        Set<Long> existingIds = new HashSet<>();
        courses.forEach(mapping -> existingIds.add(mapping.getCourse().getId()));
        selectedCourses.stream().filter(course -> !existingIds.contains(course.getId())).forEach(this::addCourse);
    }
    public void addCourse(Course course) { courses.add(NoticeCourse.create(this, course)); }
    public void addAttachment(String originalName, String storedName, String contentType, long fileSize) {
        attachments.add(NoticeAttachment.create(this, originalName, storedName, contentType, fileSize));
    }
}