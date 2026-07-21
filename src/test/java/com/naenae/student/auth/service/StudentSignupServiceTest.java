package com.naenae.student.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.common.user.domain.Role;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.common.legal.LegalConsentValidator;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.domain.CourseStudent;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.invitation.service.InvitationCodeService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class StudentSignupServiceTest {

    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private CourseStudentRepository courseStudentRepository;
    private PasswordEncoder passwordEncoder;
    private InvitationCodeService invitationCodeService;
    private StudentSignupService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        studentRepository = mock(StudentRepository.class);
        courseRepository = mock(CourseRepository.class);
        courseStudentRepository = mock(CourseStudentRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        invitationCodeService = mock(InvitationCodeService.class);
        service = new StudentSignupService(
                userRepository,
                studentRepository,
                courseRepository,
                courseStudentRepository,
                passwordEncoder,
                invitationCodeService,
                new LegalConsentValidator()
        );
    }

    @Test
    void invitationCodeLoadsTeacherCoursesAndCourseStudents() {
        Teacher teacher = teacher(1L);
        Course course = Course.create(teacher, "중등 1반");
        ReflectionTestUtils.setField(course, "id", 10L);
        Student student = Student.create(teacher, "김학생", "나래중", "010-1234-5678");
        ReflectionTestUtils.setField(student, "id", 20L);
        CourseStudent mapping = CourseStudent.create(course, student);

        when(invitationCodeService.requireActive("class-a")).thenReturn(teacher);
        when(courseRepository.findByTeacherIdOrderByTitleAsc(1L)).thenReturn(List.of(course));
        when(courseRepository.findByIdAndTeacherId(10L, 1L)).thenReturn(Optional.of(course));
        when(courseStudentRepository.findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(10L, 1L))
                .thenReturn(List.of(mapping));

        assertThat(service.getCourses("class-a")).extracting("id", "name")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(10L, "중등 1반"));
        assertThat(service.getStudents("class-a", 10L)).extracting("id", "name")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(20L, "김학생"));
    }

    @Test
    void selectedCourseStudentCreatesStudentRoleWithUniqueLoginId() {
        Teacher teacher = teacher(1L);
        Course course = Course.create(teacher, "중등 1반");
        ReflectionTestUtils.setField(course, "id", 10L);
        Student student = Student.create(teacher, "김학생", "나래중", "010-1234-5678");
        ReflectionTestUtils.setField(student, "id", 20L);

        when(invitationCodeService.requireActive("class-a")).thenReturn(teacher);
        when(courseRepository.findByIdAndTeacherId(10L, 1L)).thenReturn(Optional.of(course));
        when(studentRepository.findByIdAndTeacherId(20L, 1L)).thenReturn(Optional.of(student));
        when(courseStudentRepository.existsByCourseIdAndStudentId(10L, 20L)).thenReturn(true);
        when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.signup("class-a", 10L, 20L, "Student.01", "password1", "password1", true, true, true);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.STUDENT);
        assertThat(userCaptor.getValue().getLoginId()).isEqualTo("student.01");
        assertThat(userCaptor.getValue().getEmail()).isNull();
        assertThat(student.getUser()).isSameAs(userCaptor.getValue());
        assertThat(userCaptor.getValue().getTermsAgreedAt()).isNotNull();
        assertThat(userCaptor.getValue().getPrivacyAgreedAt()).isNotNull();
        verify(studentRepository).save(student);
        verify(invitationCodeService).consume(teacher, "class-a");
    }

    @Test
    void rejectsDuplicateLoginId() {
        Teacher teacher = teacher(1L);
        Course course = Course.create(teacher, "중등 1반");
        ReflectionTestUtils.setField(course, "id", 10L);
        Student student = Student.create(teacher, "김학생", null, null);
        ReflectionTestUtils.setField(student, "id", 20L);

        when(invitationCodeService.requireActive("class-a")).thenReturn(teacher);
        when(courseRepository.findByIdAndTeacherId(10L, 1L)).thenReturn(Optional.of(course));
        when(studentRepository.findByIdAndTeacherId(20L, 1L)).thenReturn(Optional.of(student));
        when(courseStudentRepository.existsByCourseIdAndStudentId(10L, 20L)).thenReturn(true);
        when(userRepository.existsByLoginId("student01")).thenReturn(true);

        assertThatThrownBy(() -> service.signup("class-a", 10L, 20L, "student01", "password1", "password1", true, true, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 아이디");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsStudentOutsideSelectedCourse() {
        Teacher teacher = teacher(1L);
        Course course = Course.create(teacher, "중등 1반");
        ReflectionTestUtils.setField(course, "id", 10L);
        Student student = Student.create(teacher, "김학생", null, null);
        ReflectionTestUtils.setField(student, "id", 20L);

        when(invitationCodeService.requireActive("class-a")).thenReturn(teacher);
        when(courseRepository.findByIdAndTeacherId(10L, 1L)).thenReturn(Optional.of(course));
        when(studentRepository.findByIdAndTeacherId(20L, 1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> service.signup("class-a", 10L, 20L, "student01", "password1", "password1", true, true, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("선택한 반에 등록된 학생이 아닙니다");
    }

    private Teacher teacher(Long id) {
        Teacher teacher = Teacher.create(User.createTeacher("teacher@example.com", "encoded", "김선생"));
        ReflectionTestUtils.setField(teacher, "id", id);
        return teacher;
    }
}
