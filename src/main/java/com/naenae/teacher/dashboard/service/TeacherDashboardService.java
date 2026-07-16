package com.naenae.teacher.dashboard.service;

import java.time.LocalDate;
import com.naenae.common.board.service.BoardService;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.assignment.domain.AssignmentStatus;
import com.naenae.teacher.assignment.repository.AssignmentRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.dashboard.model.TeacherDashboard;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.notice.service.TeacherNoticeService;
import com.naenae.teacher.classschedule.service.TeacherClassScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherDashboardService {
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final TeacherNoticeService teacherNoticeService;
    private final BoardService boardService;
    private final TeacherClassScheduleService classScheduleService;

    public TeacherDashboardService(TeacherRepository teacherRepository, StudentRepository studentRepository,
                                   CourseStudentRepository courseStudentRepository,
                                   AttendanceRepository attendanceRepository, AssignmentRepository assignmentRepository,
                                   TeacherNoticeService teacherNoticeService, BoardService boardService,
                                   TeacherClassScheduleService classScheduleService) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.attendanceRepository = attendanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.teacherNoticeService = teacherNoticeService;
        this.boardService = boardService;
        this.classScheduleService = classScheduleService;
    }

    @Transactional(readOnly = true)
    public TeacherDashboard getDashboard(Long teacherUserId) {
        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));

        int totalStudentCount = Math.toIntExact(studentRepository.countByTeacherId(teacher.getId()));
        int totalAttendanceTargets = Math.toIntExact(
                courseStudentRepository.countActiveEnrollmentsByTeacherId(teacher.getId())
        );
        LocalDate today = LocalDate.now();
        int presentCount = count(teacher.getId(), today, AttendanceStatus.PRESENT);
        int lateCount = count(teacher.getId(), today, AttendanceStatus.LATE);
        int absentCount = count(teacher.getId(), today, AttendanceStatus.ABSENT);
        int attendanceRate = totalAttendanceTargets == 0
                ? 0
                : Math.round((presentCount + lateCount) * 100f / totalAttendanceTargets);

        int openAssignmentCount = Math.toIntExact(
                assignmentRepository.countByTeacherIdAndStatus(teacher.getId(), AssignmentStatus.IN_PROGRESS));

        return new TeacherDashboard(totalStudentCount, presentCount, lateCount, absentCount,
                attendanceRate, openAssignmentCount, 0,
                teacherNoticeService.getDashboardNotice(teacherUserId).orElse(null),
                boardService.getRecentPosts(teacherUserId, 3),
                classScheduleService.getTodaySchedules(teacherUserId, today));
    }

    private int count(Long teacherId, LocalDate date, AttendanceStatus status) {
        return Math.toIntExact(attendanceRepository.countInActiveCoursesByTeacherAndDateAndStatus(
                teacherId, date, status
        ));
    }
}
