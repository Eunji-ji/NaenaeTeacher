package com.naenae.teacher.auth.service;

import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherSignupService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherSignupService(
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(String name, String email, String password, String passwordConfirm) {
        String normalizedName = requireText(name, "이름을 입력해 주세요.");
        String normalizedEmail = requireText(email, "이메일을 입력해 주세요.").toLowerCase();
        String rawPassword = requireText(password, "비밀번호를 입력해 주세요.");
        String rawPasswordConfirm = requireText(passwordConfirm, "비밀번호 확인을 입력해 주세요.");

        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (!rawPassword.equals(rawPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.createTeacher(
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                normalizedName
        );
        User savedUser = userRepository.save(user);
        teacherRepository.save(Teacher.create(savedUser));
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
