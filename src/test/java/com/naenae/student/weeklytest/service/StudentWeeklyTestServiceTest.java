package com.naenae.student.weeklytest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.user.domain.User;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.weeklytest.domain.WeeklyTest;
import com.naenae.teacher.weeklytest.repository.WeeklyTestRepository;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

class StudentWeeklyTestServiceTest {

    @TempDir Path tempDirectory;

    @Test
    void onlyLoadsTestsWhoseRosterContainsLoggedInStudent() {
        User teacherUser = User.createTeacher("teacher@example.com", "encoded", "김선생");
        Teacher teacher = Teacher.create(teacherUser);
        Course course = Course.create(teacher, "중등1반");
        Student student = Student.create(teacher, "김학생", null, null);
        ReflectionTestUtils.setField(student, "id", 20L);
        WeeklyTest test = WeeklyTest.create(teacher, course, "주간테스트", null);
        ReflectionTestUtils.setField(test, "id", 30L);
        test.addStudent(student);
        StudentRepository studentRepository = mock(StudentRepository.class);
        WeeklyTestRepository testRepository = mock(WeeklyTestRepository.class);
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
        when(testRepository.findDistinctByScoresStudentIdOrderByCreatedAtDescIdDesc(any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(test)));
        StudentWeeklyTestService service = new StudentWeeklyTestService(
                studentRepository, testRepository, new LocalFileStorage(), tempDirectory.toString());

        var page = service.getTests(10L, 0);

        assertThat(page.content()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(30L);
            assertThat(item.score()).isNull();
        });
    }
}
