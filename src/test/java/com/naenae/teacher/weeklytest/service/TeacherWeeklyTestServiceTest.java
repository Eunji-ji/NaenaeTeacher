package com.naenae.teacher.weeklytest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.user.domain.User;
import com.naenae.student.profile.domain.Student;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.weeklytest.domain.WeeklyTest;
import com.naenae.teacher.weeklytest.repository.WeeklyTestRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class TeacherWeeklyTestServiceTest {

    @TempDir Path tempDirectory;
    private TeacherRepository teacherRepository;
    private CourseRepository courseRepository;
    private CourseStudentRepository mappingRepository;
    private WeeklyTestRepository testRepository;
    private TeacherWeeklyTestService service;
    private Teacher teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacherRepository = mock(TeacherRepository.class);
        courseRepository = mock(CourseRepository.class);
        mappingRepository = mock(CourseStudentRepository.class);
        testRepository = mock(WeeklyTestRepository.class);
        teacher = Teacher.create(User.createTeacher("teacher@example.com", "encoded", "김선생"));
        ReflectionTestUtils.setField(teacher, "id", 1L);
        course = Course.create(teacher, "중등1반");
        ReflectionTestUtils.setField(course, "id", 2L);
        when(teacherRepository.findByUserId(10L)).thenReturn(Optional.of(teacher));
        when(courseRepository.findByIdAndTeacherId(2L, 1L)).thenReturn(Optional.of(course));
        service = new TeacherWeeklyTestService(teacherRepository, courseRepository, mappingRepository,
                testRepository, new LocalFileStorage(), tempDirectory.toString());
    }

    @Test
    void createsAutomaticNameAndStudentRosterByStableStudentId() {
        Student student = Student.create(teacher, "김학생", "나래중", null);
        ReflectionTestUtils.setField(student, "id", 20L);
        when(mappingRepository.findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(2L, 1L))
                .thenReturn(List.of(CourseStudent.create(course, student)));
        when(testRepository.save(any(WeeklyTest.class))).thenAnswer(invocation -> {
            WeeklyTest test = invocation.getArgument(0);
            ReflectionTestUtils.setField(test, "id", 30L);
            return test;
        });

        Long id = service.create(10L, 2L, "교재 3단원", List.of());

        ArgumentCaptor<WeeklyTest> captor = ArgumentCaptor.forClass(WeeklyTest.class);
        org.mockito.Mockito.verify(testRepository).save(captor.capture());
        WeeklyTest saved = captor.getValue();
        int week = (LocalDate.now().getDayOfMonth() - 1) / 7;
        String[] labels = {"첫째", "둘째", "셋째", "넷째", "다섯째"};
        assertThat(id).isEqualTo(30L);
        assertThat(saved.getName()).isEqualTo("[%d년 %02d월 %s주 테스트] 중등1반".formatted(
                LocalDate.now().getYear(), LocalDate.now().getMonthValue(), labels[Math.min(week, 4)]));
        assertThat(saved.getScores()).singleElement().satisfies(row ->
                assertThat(row.getStudent().getId()).isEqualTo(20L));
    }

    @Test
    void returnsActiveCoursesInCourseNameOrder() {
        Course laterCourse = Course.create(teacher, "중등2반");
        Course firstCourse = Course.create(teacher, "중등1반");
        ReflectionTestUtils.setField(laterCourse, "id", 3L);
        ReflectionTestUtils.setField(firstCourse, "id", 4L);
        when(courseRepository.findByTeacherIdOrderByTitleAsc(1L))
                .thenReturn(List.of(laterCourse, firstCourse));

        assertThat(service.getCourses(10L))
                .extracting(CourseOption::title)
                .containsExactly("중등1반", "중등2반");
    }

    @Test
    void rejectsScoreOutsideZeroToOneHundred() {
        WeeklyTest test = WeeklyTest.create(teacher, course, "테스트", null);
        Student student = Student.create(teacher, "김학생", null, null);
        test.addStudent(student);
        ReflectionTestUtils.setField(test.getScores().getFirst(), "id", 7L);
        when(testRepository.findByIdAndTeacherId(3L, 1L)).thenReturn(Optional.of(test));

        assertThatThrownBy(() -> service.updateScores(10L, 3L, Map.of(7L, "101")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100점 이하");
    }
}
