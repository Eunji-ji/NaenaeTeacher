package com.naenae.student.mypage.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.student.mypage.model.StudentMyPageData;
import com.naenae.student.mypage.model.StudentProfileImage;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import java.nio.file.Path;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudentMyPageService {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> PROFILE_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LocalFileStorage storage;
    private final Path profileRoot;

    public StudentMyPageService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            LocalFileStorage storage,
            @Value("${app.storage.profile-dir}") String profileDirectory
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.storage = storage;
        this.profileRoot = Path.of(profileDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public StudentMyPageData get(Long userId) {
        User user = getUser(userId);
        Student student = getStudent(userId);
        return new StudentMyPageData(
                student.getName(),
                user.getLoginId(),
                user.getProfileImageStoredName() != null
        );
    }

    @Transactional
    public void updateProfileImage(Long userId, MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("등록할 프로필 사진을 선택해 주세요.");
        }
        validateProfileImage(profileImage);
        User user = getUser(userId);
        getStudent(userId);

        String previousImage = user.getProfileImageStoredName();
        StoredFile storedFile = storage.store(profileRoot, profileImage);
        user.updateProfileImage(storedFile.storedName());
        if (previousImage != null && !previousImage.equals(storedFile.storedName())) {
            deleteAfterCommit(previousImage);
        }
    }

    @Transactional(readOnly = true)
    public StudentProfileImage image(Long userId) {
        User user = getUser(userId);
        getStudent(userId);
        if (user.getProfileImageStoredName() == null) {
            throw new IllegalArgumentException("프로필 사진이 없습니다.");
        }
        return new StudentProfileImage(
                storage.resolveExisting(profileRoot, user.getProfileImageStoredName()),
                contentType(user.getProfileImageStoredName())
        );
    }

    private void validateProfileImage(MultipartFile profileImage) {
        if (profileImage.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new IllegalArgumentException("프로필 사진은 5MB 이하여야 합니다.");
        }
        if (!PROFILE_IMAGE_TYPES.contains(profileImage.getContentType())) {
            throw new IllegalArgumentException("JPG, PNG, WEBP 이미지만 등록할 수 있습니다.");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }

    private Student getStudent(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
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
