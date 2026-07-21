package com.naenae.teacher.profile.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.common.user.domain.User;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "teachers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "academy_name", length = 150)
    private String academyName;

    @Column(name = "subject_name", length = 100)
    private String subjectName;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "invitation_code", length = 64, unique = true)
    private String invitationCode;

    @Column(name = "invitation_code_expires_at")
    private LocalDateTime invitationCodeExpiresAt;

    @Column(name = "invitation_code_use_count", nullable = false)
    private int invitationCodeUseCount;

    @Column(name = "invitation_code_max_uses", nullable = false)
    private int invitationCodeMaxUses = 100;

    public static Teacher create(User user) {
        Teacher teacher = new Teacher();
        teacher.user = user;
        return teacher;
    }

    public Long getId() {
        return id;
    }

    public void issueInvitationCode(String invitationCode, LocalDateTime expiresAt, int maxUses) {
        this.invitationCode = invitationCode;
        this.invitationCodeExpiresAt = expiresAt;
        this.invitationCodeUseCount = 0;
        this.invitationCodeMaxUses = maxUses;
    }

    public boolean canAcceptInvitation(LocalDateTime now) {
        return invitationCode != null
                && invitationCodeExpiresAt != null
                && invitationCodeExpiresAt.isAfter(now)
                && invitationCodeUseCount < invitationCodeMaxUses;
    }
}
