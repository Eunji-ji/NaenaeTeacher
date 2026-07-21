package com.naenae.student.weeklytest.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.student.weeklytest.model.StudentWeeklyTestDetail;
import com.naenae.student.weeklytest.model.StudentWeeklyTestListItem;
import com.naenae.teacher.weeklytest.domain.WeeklyTest;
import com.naenae.teacher.weeklytest.domain.WeeklyTestAttachment;
import com.naenae.teacher.weeklytest.model.WeeklyTestAttachmentItem;
import com.naenae.teacher.weeklytest.model.WeeklyTestDownload;
import com.naenae.teacher.weeklytest.repository.WeeklyTestRepository;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentWeeklyTestService {

    private final StudentRepository studentRepository;
    private final WeeklyTestRepository weeklyTestRepository;
    private final LocalFileStorage fileStorage;
    private final Path storageRoot;

    public StudentWeeklyTestService(StudentRepository studentRepository, WeeklyTestRepository weeklyTestRepository,
                                    LocalFileStorage fileStorage,
                                    @Value("${app.storage.weekly-test-dir}") String storageDirectory) {
        this.studentRepository = studentRepository;
        this.weeklyTestRepository = weeklyTestRepository;
        this.fileStorage = fileStorage;
        this.storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageView<StudentWeeklyTestListItem> getTests(Long userId, int page) {
        Student student = student(userId);
        return PaginationSupport.toView(weeklyTestRepository
                .findDistinctByScoresStudentIdOrderByCreatedAtDescIdDesc(
                        student.getId(), PaginationSupport.pageRequest(page))
                .map(test -> new StudentWeeklyTestListItem(test.getId(), test.getName(),
                        test.getCourse().getTitle(), test.getCreatedAt(), score(test, student.getId()))));
    }

    @Transactional(readOnly = true)
    public StudentWeeklyTestDetail getDetail(Long userId, Long testId) {
        Student student = student(userId);
        WeeklyTest test = visible(testId, student);
        return new StudentWeeklyTestDetail(test.getId(), test.getName(), test.getCourse().getTitle(),
                test.getRemarks(), test.getCreatedAt(), score(test, student.getId()),
                test.getAttachments().stream().map(this::attachment).toList());
    }

    @Transactional(readOnly = true)
    public WeeklyTestDownload download(Long userId, Long testId, Long attachmentId) {
        Student student = student(userId);
        WeeklyTest test = visible(testId, student);
        WeeklyTestAttachment file = test.getAttachments().stream()
                .filter(item -> item.getId().equals(attachmentId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        return new WeeklyTestDownload(fileStorage.resolveExisting(storageRoot, file.getStoredName()),
                file.getOriginalName(), file.getContentType());
    }

    private Student student(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
    }

    private WeeklyTest visible(Long testId, Student student) {
        return weeklyTestRepository.findDistinctByIdAndScoresStudentId(testId, student.getId())
                .orElseThrow(() -> new IllegalArgumentException("주간테스트를 찾을 수 없습니다."));
    }

    private Integer score(WeeklyTest test, Long studentId) {
        return test.getScores().stream().filter(row -> row.getStudent().getId().equals(studentId))
                .findFirst().map(row -> row.getScore()).orElse(null);
    }

    private WeeklyTestAttachmentItem attachment(WeeklyTestAttachment file) {
        return new WeeklyTestAttachmentItem(file.getId(), file.getOriginalName(), formatSize(file.getFileSize()));
    }

    private String formatSize(long bytes) {
        if (bytes >= 1048576) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        if (bytes >= 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
}
