package com.naenae.teacher.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.naenae.teacher.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByTeacherIdAndCourseIdAndAttendanceDateOrderByStudentNameAsc(Long teacherId, Long courseId, LocalDate attendanceDate);

    Optional<Attendance> findByTeacherIdAndCourseIdAndStudentIdAndAttendanceDate(Long teacherId, Long courseId, Long studentId, LocalDate attendanceDate);
}
