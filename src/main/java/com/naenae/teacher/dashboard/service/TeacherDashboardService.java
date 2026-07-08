package com.naenae.teacher.dashboard.service;

import java.time.LocalDate;

import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.dashboard.model.TeacherDashboard;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import org.springframework.stereotype.Service;

@Service
public class TeacherDashboardService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public TeacherDashboardService(
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public TeacherDashboard getDashboard(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));

        int totalStudentCount = Math.toIntExact(studentRepository.countByTeacherId(teacher.getId()));
        LocalDate today = LocalDate.now();
        int presentCount = Math.toIntExact(attendanceRepository.countByTeacherIdAndAttendanceDateAndStatus(
                teacher.getId(), today, AttendanceStatus.PRESENT));
        int lateCount = Math.toIntExact(attendanceRepository.countByTeacherIdAndAttendanceDateAndStatus(
                teacher.getId(), today, AttendanceStatus.LATE));
        int absentCount = Math.toIntExact(attendanceRepository.countByTeacherIdAndAttendanceDateAndStatus(
                teacher.getId(), today, AttendanceStatus.ABSENT));
        int checkedCount = presentCount + lateCount + absentCount;
        int attendanceRate = checkedCount == 0 ? 0 : Math.round((presentCount + lateCount) * 100f / checkedCount);

        return new TeacherDashboard(
                totalStudentCount,
                presentCount,
                lateCount,
                absentCount,
                attendanceRate,
                0,
                0
        );
    }
}
