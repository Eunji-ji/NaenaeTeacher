package com.naenae.teacher.classschedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import com.naenae.teacher.classschedule.repository.ClassScheduleRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TeacherClassScheduleServiceTest {
    private TeacherRepository teacherRepository;
    private CourseRepository courseRepository;
    private ClassScheduleRepository scheduleRepository;
    private TeacherClassScheduleService service;
    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacherRepository = mock(TeacherRepository.class);
        courseRepository = mock(CourseRepository.class);
        scheduleRepository = mock(ClassScheduleRepository.class);
        service = new TeacherClassScheduleService(teacherRepository, courseRepository, scheduleRepository);
        teacher = mock(Teacher.class);
        course = mock(Course.class);
        when(teacher.getId()).thenReturn(11L);
        when(course.getId()).thenReturn(22L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));
        when(courseRepository.findByIdAndTeacherId(22L, 11L)).thenReturn(Optional.of(course));
    }

    @Test
    void createsScheduleWithinTeachersCourse() {
        when(scheduleRepository.countOverlapping(11L, ScheduleWeekday.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 0))).thenReturn(0L);

        service.create(3L, 22L, ScheduleWeekday.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 0), "중등 문법");

        ArgumentCaptor<ClassSchedule> captor = ArgumentCaptor.forClass(ClassSchedule.class);
        verify(scheduleRepository).save(captor.capture());
        assertThat(captor.getValue().getTeacher()).isSameAs(teacher);
        assertThat(captor.getValue().getCourse()).isSameAs(course);
        assertThat(captor.getValue().getLessonTitle()).isEqualTo("중등 문법");
    }

    @Test
    void rejectsOverlappingSchedule() {
        when(scheduleRepository.countOverlapping(11L, ScheduleWeekday.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 0))).thenReturn(1L);

        assertThatThrownBy(() -> service.create(3L, 22L, ScheduleWeekday.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 0), "중등 문법"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deletesOnlyCurrentTeachersWholeSchedule() {
        when(scheduleRepository.deleteByTeacherId(11L)).thenReturn(4L);

        assertThat(service.deleteAll(3L)).isEqualTo(4L);
        verify(scheduleRepository).deleteByTeacherId(11L);
    }
}
