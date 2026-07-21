package com.naenae.student.auth.service;

import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.common.legal.LegalConsentValidator;
import com.naenae.common.legal.LegalDocumentVersions;
import com.naenae.student.auth.model.StudentSignupCourseOption;
import com.naenae.student.auth.model.StudentSignupStudentOption;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.domain.StudentStatus;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.invitation.service.InvitationCodeService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentSignupService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationCodeService invitationCodeService;
    private final LegalConsentValidator legalConsentValidator;

    public StudentSignupService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository,
            PasswordEncoder passwordEncoder,
            InvitationCodeService invitationCodeService,
            LegalConsentValidator legalConsentValidator
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationCodeService = invitationCodeService;
        this.legalConsentValidator = legalConsentValidator;
    }

    @Transactional(readOnly = true)
    public List<StudentSignupCourseOption> getCourses(String invitationCode) {
        Teacher teacher = getTeacher(invitationCode);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .filter(Course::isActive)
                .map(course -> new StudentSignupCourseOption(course.getId(), course.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentSignupStudentOption> getStudents(String invitationCode, Long courseId) {
        Teacher teacher = getTeacher(invitationCode);
        Course course = getCourse(teacher, courseId);
        return courseStudentRepository
                .findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(course.getId(), teacher.getId()).stream()
                .map(mapping -> mapping.getStudent())
                .filter(student -> student.getStatus() == StudentStatus.ACTIVE)
                .filter(student -> student.getUser() == null)
                .map(student -> new StudentSignupStudentOption(student.getId(), student.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(normalizeLoginId(loginId));
    }

    @Transactional
    public void signup(
            String invitationCode,
            Long courseId,
            Long studentId,
            String loginId,
            String password,
            String passwordConfirm,
            boolean termsAgreed,
            boolean privacyAgreed,
            boolean ageOrGuardianConfirmed
    ) {
        var agreedAt = legalConsentValidator.validateStudent(
                termsAgreed, privacyAgreed, ageOrGuardianConfirmed);
        Teacher teacher = getTeacher(invitationCode);
        Course course = getCourse(teacher, courseId);
        Student student = studentRepository.findByIdAndTeacherId(studentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("선택한 학생 정보를 찾을 수 없습니다."));

        if (!courseStudentRepository.existsByCourseIdAndStudentId(course.getId(), student.getId())) {
            throw new IllegalArgumentException("선택한 반에 등록된 학생이 아닙니다.");
        }
        if (student.getUser() != null) {
            throw new IllegalArgumentException("이미 회원가입을 완료한 학생입니다.");
        }

        String normalizedLoginId = normalizeLoginId(loginId);
        String rawPassword = requireText(password, "비밀번호를 입력해 주세요.");
        String rawPasswordConfirm = requireText(passwordConfirm, "비밀번호 확인을 입력해 주세요.");
        if (userRepository.existsByLoginId(normalizedLoginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!rawPassword.equals(rawPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        User user = userRepository.save(User.createStudent(
                normalizedLoginId,
                passwordEncoder.encode(rawPassword),
                student.getName(),
                student.getPhone()
        ));
        user.recordSignupConsent(
                LegalDocumentVersions.TERMS,
                LegalDocumentVersions.PRIVACY,
                agreedAt,
                true
        );
        student.connectUser(user);
        studentRepository.save(student);
        invitationCodeService.consume(teacher, invitationCode);
    }

    private Teacher getTeacher(String invitationCode) {
        return invitationCodeService.requireActive(invitationCode);
    }

    private Course getCourse(Teacher teacher, Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("반을 선택해 주세요.");
        }
        return courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .filter(Course::isActive)
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
    }

    private String normalizeLoginId(String loginId) {
        String value = requireText(loginId, "아이디를 입력해 주세요.").toLowerCase();
        if (!value.matches("[a-z0-9._-]{4,30}")) {
            throw new IllegalArgumentException("아이디는 영문 소문자, 숫자, 점, 밑줄, 하이픈으로 4~30자까지 입력해 주세요.");
        }
        return value;
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
