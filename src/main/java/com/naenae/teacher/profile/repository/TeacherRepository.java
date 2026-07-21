package com.naenae.teacher.profile.repository;

import java.util.Optional;
import java.time.LocalDateTime;

import com.naenae.teacher.profile.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);

    Optional<Teacher> findByInvitationCode(String invitationCode);

    boolean existsByInvitationCode(String invitationCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Teacher teacher
               set teacher.invitationCodeUseCount = teacher.invitationCodeUseCount + 1
             where teacher.id = :teacherId
               and teacher.invitationCode = :invitationCode
               and teacher.invitationCodeExpiresAt > :now
               and teacher.invitationCodeUseCount < teacher.invitationCodeMaxUses
            """)
    int consumeInvitation(@Param("teacherId") Long teacherId,
                          @Param("invitationCode") String invitationCode,
                          @Param("now") LocalDateTime now);
}
