package com.naenae.common.user.domain;

import java.time.LocalDateTime;

import com.naenae.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_stored_name", length = 255)
    private String profileImageStoredName;

    @Column(length = 30)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "terms_version", length = 30)
    private String termsVersion;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    @Column(name = "privacy_version", length = 30)
    private String privacyVersion;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Column(name = "age_or_guardian_confirmed_at")
    private LocalDateTime ageOrGuardianConfirmedAt;

    public static User createTeacher(String email, String passwordHash, String name) {
        User user = new User();
        user.email = email;
        user.loginId = email;
        user.passwordHash = passwordHash;
        user.role = Role.TEACHER;
        user.name = name;
        user.nickname = name;
        user.active = true;
        return user;
    }

    public static User createStudent(String loginId, String passwordHash, String name, String phone) {
        User user = new User();
        user.loginId = loginId;
        user.passwordHash = passwordHash;
        user.role = Role.STUDENT;
        user.name = name;
        user.nickname = name;
        user.phone = phone;
        user.active = true;
        return user;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getNickname() { return nickname; }
    public String getProfileImageStoredName() { return profileImageStoredName; }
    public void updateProfile(String nickname, String profileImageStoredName) {
        this.nickname = nickname;
        this.profileImageStoredName = profileImageStoredName;
    }

    public void updateProfileImage(String profileImageStoredName) {
        this.profileImageStoredName = profileImageStoredName;
    }
    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void recordSignupConsent(String termsVersion, String privacyVersion,
                                    LocalDateTime agreedAt, boolean ageOrGuardianConfirmed) {
        this.termsVersion = termsVersion;
        this.termsAgreedAt = agreedAt;
        this.privacyVersion = privacyVersion;
        this.privacyAgreedAt = agreedAt;
        this.ageOrGuardianConfirmedAt = ageOrGuardianConfirmed ? agreedAt : null;
    }
}
