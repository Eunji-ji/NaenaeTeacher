package com.naenae.student.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class StudentMyPageServiceTest {

    @TempDir
    Path tempDirectory;

    private User user;
    private Student student;
    private LocalFileStorage storage;
    private StudentMyPageService service;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        storage = mock(LocalFileStorage.class);
        user = User.createStudent("student01", "encoded", "김학생", "010-1234-5678");
        ReflectionTestUtils.setField(user, "id", 10L);
        Teacher teacher = Teacher.create(User.createTeacher("teacher@example.com", "encoded", "김선생"));
        student = Student.create(teacher, "김학생", "나래중", "010-1234-5678");
        student.connectUser(user);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
        service = new StudentMyPageService(userRepository, studentRepository, storage, tempDirectory.toString());
    }

    @Test
    void displaysRealNameAndStoresProfileImageWithoutNickname() {
        var image = new MockMultipartFile("profileImage", "student.png", "image/png", new byte[]{1, 2, 3});
        when(storage.store(org.mockito.ArgumentMatchers.any(Path.class), org.mockito.ArgumentMatchers.same(image)))
                .thenReturn(new StoredFile("student.png", "stored-student.png", "image/png", 3));

        service.updateProfileImage(10L, image);

        assertThat(user.getProfileImageStoredName()).isEqualTo("stored-student.png");
        assertThat(service.get(10L).name()).isEqualTo("김학생");
        assertThat(service.get(10L).loginId()).isEqualTo("student01");
    }

    @Test
    void replacesOldImageAndRejectsUnsupportedTypes() {
        user.updateProfileImage("old.png");
        var image = new MockMultipartFile("profileImage", "student.webp", "image/webp", new byte[]{1});
        when(storage.store(org.mockito.ArgumentMatchers.any(Path.class), org.mockito.ArgumentMatchers.same(image)))
                .thenReturn(new StoredFile("student.webp", "new.webp", "image/webp", 1));

        service.updateProfileImage(10L, image);
        verify(storage).deleteIfExists(org.mockito.ArgumentMatchers.any(Path.class), org.mockito.ArgumentMatchers.eq("old.png"));

        var invalid = new MockMultipartFile("profileImage", "student.gif", "image/gif", new byte[]{1});
        assertThatThrownBy(() -> service.updateProfileImage(10L, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPG, PNG, WEBP");
    }
}
