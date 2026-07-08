package com.naenae.teacher.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.attendance.domain.Attendance;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.model.AttendancePage;
import com.naenae.teacher.attendance.model.AttendanceRow;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherAttendanceService {

    private static final DateTimeFormatter DATE_LABEL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREA);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final AttendanceRepository attendanceRepository;

    public TeacherAttendanceService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository,
            AttendanceRepository attendanceRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public AttendancePage getPage(Long teacherUserId, Long requestedCourseId, LocalDate requestedDate) {
        Teacher teacher = getTeacher(teacherUserId);
        List<CourseOption> courses = courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();

        if (courses.isEmpty()) {
            LocalDate selectedDate = requestedDate == null ? LocalDate.now() : requestedDate;
            return new AttendancePage(
                    courses,
                    null,
                    null,
                    selectedDate,
                    selectedDate.format(DATE_LABEL_FORMATTER),
                    selectedDate.minusDays(1),
                    selectedDate.plusDays(1),
                    List.of()
            );
        }

        Long selectedCourseId = requestedCourseId != null ? requestedCourseId : courses.get(0).id();
        Course selectedCourse = courseRepository.findByIdAndTeacherId(selectedCourseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
        LocalDate selectedDate = requestedDate == null ? LocalDate.now() : requestedDate;

        List<CourseStudent> classStudents = courseStudentRepository
                .findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(selectedCourse.getId(), teacher.getId());
        List<Attendance> checkedAttendances = attendanceRepository
                .findByTeacherIdAndCourseIdAndAttendanceDateOrderByStudentNameAsc(teacher.getId(), selectedCourse.getId(), selectedDate);

        Map<Long, Attendance> attendanceMap = new HashMap<>();
        for (Attendance attendance : checkedAttendances) {
            attendanceMap.put(attendance.getStudent().getId(), attendance);
        }

        List<AttendanceRow> students = classStudents.stream()
                .map(mapping -> {
                    Student student = mapping.getStudent();
                    Attendance attendance = attendanceMap.get(student.getId());
                    return new AttendanceRow(
                            student.getId(),
                            student.getName(),
                            student.getSchoolName(),
                            attendance == null ? null : attendance.getStatus().name(),
                            attendance == null ? null : attendance.getCheckedAt()
                    );
                })
                .toList();

        return new AttendancePage(
                courses,
                selectedCourse.getId(),
                selectedCourse.getTitle(),
                selectedDate,
                selectedDate.format(DATE_LABEL_FORMATTER),
                selectedDate.minusDays(1),
                selectedDate.plusDays(1),
                students
        );
    }

    @Transactional
    public void saveAttendance(Long teacherUserId, Long courseId, LocalDate attendanceDate, Long studentId, AttendanceStatus status) {
        Teacher teacher = getTeacher(teacherUserId);
        if (courseId == null) {
            throw new IllegalArgumentException("반을 선택해 주세요.");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("학생을 선택해 주세요.");
        }
        if (status == null) {
            attendanceRepository.findByTeacherIdAndCourseIdAndStudentIdAndAttendanceDate(
                    teacher.getId(), courseId, studentId, attendanceDate
            ).ifPresent(attendanceRepository::delete);
            return;
        }

        Course course = courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
        List<CourseStudent> classStudents = courseStudentRepository
                .findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(course.getId(), teacher.getId());
        Map<Long, Student> studentsById = new HashMap<>();
        for (CourseStudent mapping : classStudents) {
            studentsById.put(mapping.getStudent().getId(), mapping.getStudent());
        }
        Student student = studentsById.get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("선택한 반에 속한 학생만 저장할 수 있습니다.");
        }

        LocalDateTime checkedAt = LocalDateTime.now();
        Attendance attendance = attendanceRepository
                .findByTeacherIdAndCourseIdAndStudentIdAndAttendanceDate(teacher.getId(), course.getId(), studentId, attendanceDate)
                .orElseGet(() -> Attendance.create(teacher, course, student, attendanceDate, status, checkedAt));

        attendance.updateStatus(status, checkedAt);
        attendanceRepository.save(attendance);
    }

    public String formatCheckedAt(LocalDateTime checkedAt) {
        return checkedAt == null ? "" : checkedAt.format(TIME_FORMATTER);
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }
}
