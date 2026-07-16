package com.naenae.teacher.classprogress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.teacher.classprogress.domain.ClassProgressNote;
import com.naenae.teacher.classprogress.repository.ClassProgressNoteRepository;
import com.naenae.teacher.classschedule.domain.ClassSchedule;
import com.naenae.teacher.classschedule.repository.ClassScheduleRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class TeacherClassProgressServiceTest {
    private TeacherRepository teacherRepository;
    private CourseRepository courseRepository;
    private ClassScheduleRepository scheduleRepository;
    private ClassProgressNoteRepository noteRepository;
    private TeacherClassProgressService service;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacherRepository = mock(TeacherRepository.class);
        courseRepository = mock(CourseRepository.class);
        scheduleRepository = mock(ClassScheduleRepository.class);
        noteRepository = mock(ClassProgressNoteRepository.class);
        service = new TeacherClassProgressService(
                teacherRepository, courseRepository, scheduleRepository, noteRepository);
        teacher = mock(Teacher.class);
        when(teacher.getId()).thenReturn(11L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));
    }

    @Test
    void createsFreeMemoWithoutCourseOrSchedule() {
        service.create(3L, null, null, "  다음 시간에는 24쪽부터  ");

        ArgumentCaptor<ClassProgressNote> captor = ArgumentCaptor.forClass(ClassProgressNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getCourse()).isNull();
        assertThat(captor.getValue().getClassSchedule()).isNull();
        assertThat(captor.getValue().getMemo()).isEqualTo("다음 시간에는 24쪽부터");
        assertThat(captor.getValue().getNoteColor()).isNotNull();
    }

    @Test
    void keepsCourseAndLessonLabelsFromSelectedSchedule() {
        Course course = mock(Course.class);
        ClassSchedule schedule = mock(ClassSchedule.class);
        when(course.getId()).thenReturn(22L);
        when(course.getTitle()).thenReturn("중등1반");
        when(schedule.getCourse()).thenReturn(course);
        when(schedule.getLessonTitle()).thenReturn("영어문법");
        when(courseRepository.findByIdAndTeacherId(22L, 11L)).thenReturn(Optional.of(course));
        when(scheduleRepository.findByIdAndTeacherId(33L, 11L)).thenReturn(Optional.of(schedule));

        service.create(3L, 22L, 33L, "현재완료까지 진행");

        ArgumentCaptor<ClassProgressNote> captor = ArgumentCaptor.forClass(ClassProgressNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getCourseTitle()).isEqualTo("중등1반");
        assertThat(captor.getValue().getLessonTitle()).isEqualTo("영어문법");
    }

    @Test
    void rejectsScheduleFromAnotherSelectedCourse() {
        Course selectedCourse = mock(Course.class);
        Course scheduleCourse = mock(Course.class);
        ClassSchedule schedule = mock(ClassSchedule.class);
        when(selectedCourse.getId()).thenReturn(22L);
        when(scheduleCourse.getId()).thenReturn(44L);
        when(schedule.getCourse()).thenReturn(scheduleCourse);
        when(courseRepository.findByIdAndTeacherId(22L, 11L)).thenReturn(Optional.of(selectedCourse));
        when(scheduleRepository.findByIdAndTeacherId(33L, 11L)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> service.create(3L, 22L, 33L, "진도 메모"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당하는 시간표");
    }

    @Test
    void rejectsMemoLongerThanOneThousandCharacters() {
        assertThatThrownBy(() -> service.create(3L, null, null, "가".repeat(1001)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1,000자");
    }

    @Test
    void queriesSixNewestNotesPerPage() {
        when(noteRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                org.mockito.ArgumentMatchers.eq(11L), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        service.getNotes(3L, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(noteRepository).findByTeacherIdOrderByCreatedAtDescIdDesc(
                org.mockito.ArgumentMatchers.eq(11L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(6);
        assertThat(captor.getValue().getPageNumber()).isZero();
    }
}
