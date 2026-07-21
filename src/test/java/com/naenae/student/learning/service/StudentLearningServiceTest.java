package com.naenae.student.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.naenae.common.user.domain.User;
import com.naenae.student.profile.domain.AcademicExamType;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.domain.StudentAcademicRecord;
import com.naenae.student.profile.repository.StudentAcademicRecordRepository;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.attendance.domain.Attendance;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.repository.AttendanceRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.wordtest.domain.WordTest;
import com.naenae.teacher.wordtest.repository.WordTestRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

class StudentLearningServiceTest {

    private StudentRepository studentRepository;
    private StudentAcademicRecordRepository academicRecordRepository;
    private CourseStudentRepository courseStudentRepository;
    private WordTestRepository wordTestRepository;
    private AttendanceRepository attendanceRepository;
    private StudentLearningService service;
    private Teacher teacher;
    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        academicRecordRepository = mock(StudentAcademicRecordRepository.class);
        courseStudentRepository = mock(CourseStudentRepository.class);
        wordTestRepository = mock(WordTestRepository.class);
        attendanceRepository = mock(AttendanceRepository.class);
        service = new StudentLearningService(
                studentRepository,
                academicRecordRepository,
                courseStudentRepository,
                wordTestRepository,
                attendanceRepository
        );

        teacher = Teacher.create(User.createTeacher("teacher@example.com", "encoded", "김선생"));
        ReflectionTestUtils.setField(teacher, "id", 1L);
        student = Student.create(teacher, "김학생", "나래중", "010-1234-5678");
        ReflectionTestUtils.setField(student, "id", 2L);
        course = Course.create(teacher, "중등 1반");
        ReflectionTestUtils.setField(course, "id", 3L);
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
    }

    @Test
    void scoreChangesFollowYearMidtermFinalOrder() {
        List<StudentAcademicRecord> records = List.of(
                StudentAcademicRecord.create(teacher, student, 2026, AcademicExamType.FINAL, 100),
                StudentAcademicRecord.create(teacher, student, 2025, AcademicExamType.FINAL, 80),
                StudentAcademicRecord.create(teacher, student, 2026, AcademicExamType.MIDTERM, 90),
                StudentAcademicRecord.create(teacher, student, 2025, AcademicExamType.MIDTERM, 70)
        );
        when(academicRecordRepository.findByStudentIdAndStudentTeacherIdOrderByExamYearAscExamTypeAsc(2L, 1L))
                .thenReturn(records);

        var page = service.getScores(10L);

        assertThat(page.scores()).extracting("examYear", "examLabel", "score", "changeFromPrevious")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(2025, "중간고사", 70, null),
                        org.assertj.core.groups.Tuple.tuple(2025, "기말고사", 80, 10),
                        org.assertj.core.groups.Tuple.tuple(2026, "중간고사", 90, 10),
                        org.assertj.core.groups.Tuple.tuple(2026, "기말고사", 100, 10)
                );
        assertThat(page.latestScore()).isEqualTo(100);
        assertThat(page.latestChange()).isEqualTo(10);
    }

    @Test
    void wordTestsAreLimitedToStudentsCourses() {
        WordTest test = WordTest.create(teacher, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        ReflectionTestUtils.setField(test, "id", 4L);
        test.addCourse(course);
        test.addWord(1, "apple", "사과");
        when(courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(2L))
                .thenReturn(List.of(CourseStudent.create(course, student)));
        when(wordTestRepository.findStudentTests(org.mockito.ArgumentMatchers.eq(List.of(3L)), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(test)));

        var page = service.getWordTests(10L, 0);

        assertThat(page.content()).singleElement().satisfies(item -> {
            assertThat(item.courseNames()).isEqualTo("중등 1반");
            assertThat(item.wordCount()).isEqualTo(1);
            assertThat(item.statusLabel()).isEqualTo("진행 중");
        });
    }

    @Test
    void attendanceRateIncludesPresentAndLate() {
        List<Attendance> records = List.of(
                attendance(LocalDate.now().minusDays(2), AttendanceStatus.PRESENT),
                attendance(LocalDate.now().minusDays(1), AttendanceStatus.LATE),
                attendance(LocalDate.now(), AttendanceStatus.ABSENT)
        );
        when(attendanceRepository.findByStudentIdAndTeacherId(2L, 1L)).thenReturn(records);
        when(attendanceRepository.findByStudentIdAndTeacherIdOrderByAttendanceDateDescIdDesc(
                org.mockito.ArgumentMatchers.eq(2L), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(records));

        var page = service.getAttendance(10L, 0);

        assertThat(page.attendanceRate()).isEqualTo(67);
        assertThat(page.totalCount()).isEqualTo(3);
        assertThat(page.presentCount()).isEqualTo(1);
        assertThat(page.lateCount()).isEqualTo(1);
        assertThat(page.absentCount()).isEqualTo(1);
    }

    private Attendance attendance(LocalDate date, AttendanceStatus status) {
        return Attendance.create(teacher, course, student, date, status, LocalDateTime.now());
    }
}
