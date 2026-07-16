package com.naenae.teacher.classprogress.service;

import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.teacher.classprogress.domain.ClassProgressNote;
import com.naenae.teacher.classprogress.domain.ProgressNoteColor;
import com.naenae.teacher.classprogress.model.ClassProgressNoteItem;
import com.naenae.teacher.classprogress.model.ProgressScheduleOption;
import com.naenae.teacher.classprogress.repository.ClassProgressNoteRepository;
import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.classschedule.repository.ClassScheduleRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherClassProgressService {
    private static final int PROGRESS_PAGE_SIZE = 6;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final ClassScheduleRepository scheduleRepository;
    private final ClassProgressNoteRepository noteRepository;

    public TeacherClassProgressService(TeacherRepository teacherRepository, CourseRepository courseRepository,
                                       ClassScheduleRepository scheduleRepository,
                                       ClassProgressNoteRepository noteRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.scheduleRepository = scheduleRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long userId) {
        Teacher teacher = teacher(userId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .filter(Course::isActive)
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressScheduleOption> getSchedules(Long userId) {
        Teacher teacher = teacher(userId);
        return scheduleRepository.findByTeacherIdOrderByWeekdayAscStartTimeAscEndTimeAsc(teacher.getId()).stream()
                .map(schedule -> new ProgressScheduleOption(schedule.getId(), schedule.getCourse().getId(),
                        schedule.getWeekday().getLabel(), schedule.getStartTime(), schedule.getEndTime(),
                        schedule.getLessonTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageView<ClassProgressNoteItem> getNotes(Long userId, int page) {
        Teacher teacher = teacher(userId);
        return PaginationSupport.toView(noteRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                teacher.getId(), PaginationSupport.pageRequest(page, PROGRESS_PAGE_SIZE))
                .map(note -> new ClassProgressNoteItem(note.getId(), note.getCourseTitle(), note.getLessonTitle(),
                        note.getNoteColor(), note.getMemo(), note.getCreatedAt())));
    }

    @Transactional
    public void create(Long userId, Long courseId, Long scheduleId, String memo) {
        Teacher teacher = teacher(userId);
        String normalizedMemo = memo == null ? "" : memo.trim();
        if (normalizedMemo.isEmpty()) {
            throw new IllegalArgumentException("진도 메모를 입력해 주세요.");
        }
        if (normalizedMemo.length() > 1000) {
            throw new IllegalArgumentException("진도 메모는 1,000자 이하로 입력해 주세요.");
        }

        Course course = courseId == null ? null : courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
        ClassSchedule schedule = scheduleId == null ? null
                : scheduleRepository.findByIdAndTeacherId(scheduleId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 시간표를 찾을 수 없습니다."));

        if (schedule != null) {
            if (course != null && !schedule.getCourse().getId().equals(course.getId())) {
                throw new IllegalArgumentException("선택한 반에 해당하는 시간표를 선택해 주세요.");
            }
            course = schedule.getCourse();
        }

        String courseTitle = course == null ? null : course.getTitle();
        String lessonTitle = schedule == null ? null : schedule.getLessonTitle();
        noteRepository.save(ClassProgressNote.create(
                teacher, course, schedule, courseTitle, lessonTitle, ProgressNoteColor.random(), normalizedMemo));
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        Teacher teacher = teacher(userId);
        ClassProgressNote note = noteRepository.findByIdAndTeacherId(noteId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 진도 메모를 찾을 수 없습니다."));
        noteRepository.delete(note);
    }

    private Teacher teacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }
}
