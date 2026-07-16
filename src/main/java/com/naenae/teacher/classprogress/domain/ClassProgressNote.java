package com.naenae.teacher.classprogress.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.Column;
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

@Getter
@Entity
@Table(name = "class_progress_notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassProgressNote extends BaseTimeEntity {
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
    @JoinColumn(name = "class_schedule_id")
    private ClassSchedule classSchedule;

    @Column(name = "course_title", length = 150)
    private String courseTitle;

    @Column(name = "lesson_title", length = 150)
    private String lessonTitle;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "note_color", nullable = false, length = 20)
    private ProgressNoteColor noteColor;

    @Column(nullable = false, length = 1000)
    private String memo;

    public static ClassProgressNote create(Teacher teacher, Course course, ClassSchedule classSchedule,
                                           String courseTitle, String lessonTitle,
                                           ProgressNoteColor noteColor, String memo) {
        ClassProgressNote note = new ClassProgressNote();
        note.teacher = teacher;
        note.course = course;
        note.classSchedule = classSchedule;
        note.courseTitle = courseTitle;
        note.lessonTitle = lessonTitle;
        note.noteColor = noteColor;
        note.memo = memo;
        return note;
    }
}
