package com.naenae.teacher.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.naenae.teacher.attendance.domain.Attendance;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByTeacherIdAndCourseIdAndAttendanceDateOrderByStudentNameAsc(Long teacherId, Long courseId, LocalDate attendanceDate);

    Optional<Attendance> findByTeacherIdAndCourseIdAndStudentIdAndAttendanceDate(Long teacherId, Long courseId, Long studentId, LocalDate attendanceDate);

    long countByTeacherIdAndAttendanceDateAndStatus(Long teacherId, LocalDate attendanceDate, AttendanceStatus status);

    @Query("""
            select count(attendance) from Attendance attendance
            where attendance.teacher.id = :teacherId
              and attendance.course.active = true
              and attendance.attendanceDate = :attendanceDate
              and attendance.status = :status
            """)
    long countInActiveCoursesByTeacherAndDateAndStatus(
            @Param("teacherId") Long teacherId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("status") AttendanceStatus status
    );

    Page<Attendance> findByStudentIdAndTeacherIdOrderByAttendanceDateDescIdDesc(
            Long studentId,
            Long teacherId,
            Pageable pageable
    );

    List<Attendance> findByStudentIdAndTeacherId(Long studentId, Long teacherId);
}
