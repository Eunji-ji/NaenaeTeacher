package com.naenae.teacher.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.assignment.repository.AssignmentRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.dashboard.model.TeacherDashboard;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TeacherDashboardServiceTest {
    @Test
    void attendanceRateUsesEveryActiveCourseEnrollmentIncludingUncheckedCourses() {
        TeacherRepository teacherRepository = mock(TeacherRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseStudentRepository courseStudentRepository = mock(CourseStudentRepository.class);
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        Teacher teacher = mock(Teacher.class);
        when(teacher.getId()).thenReturn(7L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));
        when(studentRepository.countByTeacherId(7L)).thenReturn(20L);
        when(courseStudentRepository.countActiveEnrollmentsByTeacherId(7L)).thenReturn(20L);
        when(attendanceRepository.countInActiveCoursesByTeacherAndDateAndStatus(eq(7L), any(LocalDate.class), eq(AttendanceStatus.PRESENT))).thenReturn(10L);
        when(attendanceRepository.countInActiveCoursesByTeacherAndDateAndStatus(eq(7L), any(LocalDate.class), eq(AttendanceStatus.LATE))).thenReturn(0L);
        when(attendanceRepository.countInActiveCoursesByTeacherAndDateAndStatus(eq(7L), any(LocalDate.class), eq(AttendanceStatus.ABSENT))).thenReturn(0L);

        TeacherDashboard dashboard = new TeacherDashboardService(teacherRepository, studentRepository,
                courseStudentRepository, attendanceRepository, assignmentRepository).getDashboard(3L);

        assertThat(dashboard.todayAttendanceRate()).isEqualTo(50);
        assertThat(dashboard.todayPresentCount()).isEqualTo(10);
    }

    @Test
    void attendanceRateIsZeroWhenNoCourseHasStudents() {
        TeacherRepository teacherRepository = mock(TeacherRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseStudentRepository courseStudentRepository = mock(CourseStudentRepository.class);
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        Teacher teacher = mock(Teacher.class);
        when(teacher.getId()).thenReturn(7L);
        when(teacherRepository.findByUserId(3L)).thenReturn(Optional.of(teacher));

        TeacherDashboard dashboard = new TeacherDashboardService(teacherRepository, studentRepository,
                courseStudentRepository, attendanceRepository, assignmentRepository).getDashboard(3L);

        assertThat(dashboard.todayAttendanceRate()).isZero();
    }
}