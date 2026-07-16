package com.naenae.teacher.classschedule.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "class_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassSchedule extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "teacher_id", nullable = false) private Teacher teacher;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false) private Course course;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ScheduleWeekday weekday;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(name = "lesson_title", nullable = false, length = 150) private String lessonTitle;

    public static ClassSchedule create(Teacher teacher, Course course, ScheduleWeekday weekday,
                                       LocalTime startTime, LocalTime endTime, String lessonTitle) {
        ClassSchedule schedule = new ClassSchedule();
        schedule.teacher = teacher;
        schedule.course = course;
        schedule.weekday = weekday;
        schedule.startTime = startTime;
        schedule.endTime = endTime;
        schedule.lessonTitle = lessonTitle;
        return schedule;
    }
}
