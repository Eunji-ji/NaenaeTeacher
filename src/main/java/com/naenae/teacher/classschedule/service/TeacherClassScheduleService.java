package com.naenae.teacher.classschedule.service;

import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import com.naenae.teacher.classschedule.model.ClassScheduleItem;
import com.naenae.teacher.classschedule.model.ScheduleDayColumn;
import com.naenae.teacher.classschedule.repository.ClassScheduleRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherClassScheduleService {
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final ClassScheduleRepository scheduleRepository;

    public TeacherClassScheduleService(TeacherRepository teacherRepository, CourseRepository courseRepository,
                                       ClassScheduleRepository scheduleRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long userId) {
        Teacher teacher = teacher(userId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .filter(Course::isActive).map(course -> new CourseOption(course.getId(), course.getTitle())).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleDayColumn> getWeeklySchedule(Long userId) {
        Teacher teacher = teacher(userId);
        Map<ScheduleWeekday, List<ClassScheduleItem>> grouped = scheduleRepository
                .findByTeacherIdOrderByWeekdayAscStartTimeAscEndTimeAsc(teacher.getId()).stream()
                .map(this::toItem).collect(Collectors.groupingBy(ClassScheduleItem::weekday));
        return Arrays.stream(ScheduleWeekday.values())
                .map(day -> new ScheduleDayColumn(day, day.getLabel(), day.getShortLabel(),
                        grouped.getOrDefault(day, List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassScheduleItem> getTodaySchedules(Long userId, LocalDate date) {
        Teacher teacher = teacher(userId);
        ScheduleWeekday weekday = from(date.getDayOfWeek());
        if (weekday == null) return List.of();
        return scheduleRepository.findByTeacherIdOrderByWeekdayAscStartTimeAscEndTimeAsc(teacher.getId()).stream()
                .filter(schedule -> schedule.getWeekday() == weekday).map(this::toItem).toList();
    }

    @Transactional
    public void create(Long userId, Long courseId, ScheduleWeekday weekday,
                       LocalTime startTime, LocalTime endTime, String lessonTitle) {
        Teacher teacher = teacher(userId);
        Course course = courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
        String title = lessonTitle == null ? "" : lessonTitle.trim();
        if (weekday == null) throw new IllegalArgumentException("요일을 선택해 주세요.");
        if (startTime == null || endTime == null) throw new IllegalArgumentException("수업 시간을 입력해 주세요.");
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        if (title.isEmpty()) throw new IllegalArgumentException("수업명을 입력해 주세요.");
        if (title.length() > 150) throw new IllegalArgumentException("수업명은 150자 이하여야 합니다.");
        if (scheduleRepository.countOverlapping(teacher.getId(), weekday, startTime, endTime) > 0) {
            throw new IllegalArgumentException("선택한 요일과 시간에 이미 등록된 수업이 있습니다.");
        }
        scheduleRepository.save(ClassSchedule.create(teacher, course, weekday, startTime, endTime, title));
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        Teacher teacher = teacher(userId);
        ClassSchedule schedule = scheduleRepository.findByIdAndTeacherId(scheduleId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 시간표를 찾을 수 없습니다."));
        scheduleRepository.delete(schedule);
    }

    @Transactional
    public long deleteAll(Long userId) {
        Teacher teacher = teacher(userId);
        return scheduleRepository.deleteByTeacherId(teacher.getId());
    }

    private ClassScheduleItem toItem(ClassSchedule schedule) {
        return new ClassScheduleItem(schedule.getId(), schedule.getCourse().getId(), schedule.getCourse().getTitle(),
                schedule.getWeekday(), schedule.getWeekday().getLabel(), schedule.getLessonTitle(),
                schedule.getStartTime(), schedule.getEndTime());
    }

    private ScheduleWeekday from(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> ScheduleWeekday.MONDAY;
            case TUESDAY -> ScheduleWeekday.TUESDAY;
            case WEDNESDAY -> ScheduleWeekday.WEDNESDAY;
            case THURSDAY -> ScheduleWeekday.THURSDAY;
            case FRIDAY -> ScheduleWeekday.FRIDAY;
            default -> null;
        };
    }

    private Teacher teacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }
}
