package com.naenae.teacher.invitation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InvitationCodeServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-21T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    private TeacherRepository repository;
    private InvitationCodeService service;

    @BeforeEach
    void setUp() {
        repository = mock(TeacherRepository.class);
        SecureRandom random = mock(SecureRandom.class);
        when(random.nextInt(anyInt())).thenReturn(0);
        service = new InvitationCodeService(repository, random, CLOCK, 30, 100);
    }

    @Test
    void issuesLongLivedServerGeneratedCode() {
        Teacher teacher = teacher(1L);

        service.reissue(teacher);

        assertThat(teacher.getInvitationCode()).isEqualTo("A".repeat(24));
        assertThat(teacher.getInvitationCodeExpiresAt())
                .isEqualTo(LocalDateTime.now(CLOCK).plusDays(30));
        assertThat(teacher.getInvitationCodeUseCount()).isZero();
        assertThat(teacher.getInvitationCodeMaxUses()).isEqualTo(100);
    }

    @Test
    void rejectsExpiredCodeWithGenericMessage() {
        Teacher teacher = teacher(1L);
        teacher.issueInvitationCode("A".repeat(24), LocalDateTime.now(CLOCK).minusSeconds(1), 100);
        when(repository.findByInvitationCode("A".repeat(24))).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> service.requireActive("A".repeat(24)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않거나 만료된");
    }

    @Test
    void consumesCodeWithAtomicRepositoryUpdate() {
        Teacher teacher = teacher(7L);
        when(repository.consumeInvitation(any(), any(), any())).thenReturn(1);

        service.consume(teacher, "A".repeat(24));

        verify(repository).consumeInvitation(any(), any(), any());
    }

    private Teacher teacher(Long id) {
        Teacher teacher = Teacher.create(User.createTeacher("teacher@example.com", "encoded", "김선생"));
        ReflectionTestUtils.setField(teacher, "id", id);
        return teacher;
    }
}
