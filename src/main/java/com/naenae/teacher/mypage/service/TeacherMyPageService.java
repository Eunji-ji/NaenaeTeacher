package com.naenae.teacher.mypage.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.teacher.mypage.model.MyPageData;
import com.naenae.teacher.mypage.model.ProfileImage;
import com.naenae.teacher.invitation.service.InvitationCodeService;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.nio.file.Path;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherMyPageService {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> PROFILE_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final LocalFileStorage storage;
    private final Path profileRoot;
    private final InvitationCodeService invitationCodeService;

    public TeacherMyPageService(
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            LocalFileStorage storage,
            @Value("${app.storage.profile-dir}") String profileDirectory,
            InvitationCodeService invitationCodeService
    ) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.storage = storage;
        this.profileRoot = Path.of(profileDirectory).toAbsolutePath().normalize();
        this.invitationCodeService = invitationCodeService;
    }

    @Transactional
    public MyPageData get(Long userId) {
        User user = getUser(userId);
        Teacher teacher = getTeacher(userId);
        invitationCodeService.ensureActive(teacher);
        return new MyPageData(
                user.getName(),
                user.getEmail(),
                nickname(user),
                user.getProfileImageStoredName() != null,
                teacher.getInvitationCode(),
                teacher.getInvitationCodeExpiresAt(),
                Math.max(0, teacher.getInvitationCodeMaxUses() - teacher.getInvitationCodeUseCount())
        );
    }

    @Transactional
    public void update(Long userId, String nickname, MultipartFile profileImage) {
        User user = getUser(userId);
        String normalizedNickname = normalizeNickname(nickname);

        String previousImage = user.getProfileImageStoredName();
        String nextImage = previousImage;
        if (profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);
            StoredFile storedFile = storage.store(profileRoot, profileImage);
            nextImage = storedFile.storedName();
        }

        user.updateProfile(normalizedNickname, nextImage);
        if (previousImage != null && !previousImage.equals(nextImage)) {
            deleteAfterCommit(previousImage);
        }
    }

    @Transactional
    public void reissueInvitationCode(Long userId) {
        invitationCodeService.reissue(getTeacher(userId));
    }

    @Transactional(readOnly = true)
    public ProfileImage image(Long userId) {
        User user = getUser(userId);
        if (user.getProfileImageStoredName() == null) {
            throw new IllegalArgumentException("프로필 사진이 없습니다.");
        }
        return new ProfileImage(
                storage.resolveExisting(profileRoot, user.getProfileImageStoredName()),
                contentType(user.getProfileImageStoredName())
        );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }

    private Teacher getTeacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private String normalizeNickname(String nickname) {
        String value = nickname == null ? "" : nickname.trim();
        if (value.isEmpty() || value.length() > 100) {
            throw new IllegalArgumentException("닉네임은 1자 이상 100자 이하로 입력해 주세요.");
        }
        return value;
    }

    private void validateProfileImage(MultipartFile profileImage) {
        if (profileImage.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new IllegalArgumentException("프로필 사진은 5MB 이하여야 합니다.");
        }
        if (!PROFILE_IMAGE_TYPES.contains(profileImage.getContentType())) {
            throw new IllegalArgumentException("JPG, PNG, WEBP 이미지만 등록할 수 있습니다.");
        }
    }

    private String nickname(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getName() : user.getNickname();
    }

    private String contentType(String storedName) {
        String value = storedName.toLowerCase();
        if (value.endsWith(".png")) {
            return "image/png";
        }
        if (value.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private void deleteAfterCommit(String storedName) {
        Runnable cleanup = () -> storage.deleteIfExists(profileRoot, storedName);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cleanup.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cleanup.run();
            }
        });
    }
}
