package com.naenae.teacher.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.invitation.service.InvitationCodeService;
import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TeacherMyPageServiceTest {

    @TempDir
    Path tempDirectory;

    private UserRepository userRepository;
    private TeacherRepository teacherRepository;
    private User user;
    private Teacher teacher;
    private TeacherMyPageService service;
    private InvitationCodeService invitationCodeService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        teacherRepository = mock(TeacherRepository.class);
        user = User.createTeacher("teacher@example.com", "encoded", "김선생");
        teacher = Teacher.create(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacher));
        invitationCodeService = mock(InvitationCodeService.class);
        doAnswer(invocation -> {
            teacher.issueInvitationCode("ABCDEFGHJKLMNPQRSTUV2345", LocalDateTime.now().plusDays(30), 100);
            return null;
        }).when(invitationCodeService).ensureActive(teacher);
        service = new TeacherMyPageService(
                userRepository,
                teacherRepository,
                mock(LocalFileStorage.class),
                tempDirectory.toString(),
                invitationCodeService
        );
    }

    @Test
    void savesProfileAndProvidesServerGeneratedInvitationCode() {
        service.update(1L, "나나", null);

        assertThat(user.getNickname()).isEqualTo("나나");
        assertThat(service.get(1L).invitationCode()).hasSize(24);
    }

    @Test
    void reissuesInvitationCodeThroughDedicatedAction() {
        service.reissueInvitationCode(1L);
        verify(invitationCodeService).reissue(teacher);
    }
}
