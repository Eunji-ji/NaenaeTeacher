package com.naenae.teacher.weeklytest.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.student.profile.domain.StudentStatus;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import com.naenae.teacher.weeklytest.domain.WeeklyTest;
import com.naenae.teacher.weeklytest.domain.WeeklyTestAttachment;
import com.naenae.teacher.weeklytest.domain.WeeklyTestScore;
import com.naenae.teacher.weeklytest.model.*;
import com.naenae.teacher.weeklytest.repository.WeeklyTestRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherWeeklyTestService {

    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_REMARKS_LENGTH = 2000;

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final WeeklyTestRepository weeklyTestRepository;
    private final LocalFileStorage fileStorage;
    private final Path storageRoot;

    public TeacherWeeklyTestService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            CourseStudentRepository courseStudentRepository,
            WeeklyTestRepository weeklyTestRepository,
            LocalFileStorage fileStorage,
            @Value("${app.storage.weekly-test-dir}") String storageDirectory
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.weeklyTestRepository = weeklyTestRepository;
        this.fileStorage = fileStorage;
        this.storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long userId) {
        Teacher teacher = teacher(userId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .filter(Course::isActive)
                .sorted(Comparator.comparing(Course::getTitle))
                .map(course -> new CourseOption(course.getId(), course.getTitle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageView<WeeklyTestListItem> getTests(Long userId, LocalDate startDate, LocalDate endDate, int page) {
        validatePeriod(startDate, endDate);
        Teacher teacher = teacher(userId);
        return PaginationSupport.toView(weeklyTestRepository
                .findByTeacherIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(
                        teacher.getId(), startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(),
                        PaginationSupport.pageRequest(page))
                .map(test -> new WeeklyTestListItem(
                        test.getId(), test.getName(), test.getCourse().getTitle(),
                        averageScore(test), test.getCreatedAt())));
    }

    @Transactional
    public Long create(Long userId, Long courseId, String remarks, List<MultipartFile> files) {
        Teacher teacher = teacher(userId);
        Course course = course(courseId, teacher);
        String normalizedRemarks = normalizeRemarks(remarks);
        List<MultipartFile> actualFiles = validateFiles(files);
        WeeklyTest test = WeeklyTest.create(teacher, course, testName(LocalDate.now(), course.getTitle()), normalizedRemarks);

        courseStudentRepository.findByCourseIdAndStudentTeacherIdOrderByStudentNameAsc(course.getId(), teacher.getId())
                .stream()
                .map(mapping -> mapping.getStudent())
                .filter(student -> student.getStatus() == StudentStatus.ACTIVE)
                .forEach(test::addStudent);

        actualFiles.forEach(file -> {
            StoredFile stored = fileStorage.store(storageRoot, file);
            deleteOnRollback(stored.storedName());
            test.addAttachment(stored.originalName(), stored.storedName(), stored.contentType(), stored.size());
        });
        return weeklyTestRepository.save(test).getId();
    }

    @Transactional(readOnly = true)
    public WeeklyTestDetail getDetail(Long userId, Long testId) {
        WeeklyTest test = owned(teacher(userId), testId);
        return detail(test);
    }

    @Transactional
    public void updateScores(Long userId, Long testId, Map<Long, String> submittedScores) {
        WeeklyTest test = owned(teacher(userId), testId);
        for (WeeklyTestScore row : test.getScores()) {
            if (submittedScores.containsKey(row.getId())) {
                row.updateScore(parseScore(submittedScores.get(row.getId())));
            }
        }
    }

    @Transactional(readOnly = true)
    public WeeklyTestDownload download(Long userId, Long testId, Long attachmentId) {
        WeeklyTest test = owned(teacher(userId), testId);
        WeeklyTestAttachment attachment = test.getAttachments().stream()
                .filter(item -> item.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        return new WeeklyTestDownload(
                fileStorage.resolveExisting(storageRoot, attachment.getStoredName()),
                attachment.getOriginalName(), attachment.getContentType());
    }

    private WeeklyTestDetail detail(WeeklyTest test) {
        return new WeeklyTestDetail(
                test.getId(), test.getName(), test.getCourse().getTitle(), test.getRemarks(), test.getCreatedAt(),
                test.getAttachments().stream().map(this::attachment).toList(),
                test.getScores().stream()
                        .sorted(Comparator.comparing(row -> row.getStudent().getName(), String.CASE_INSENSITIVE_ORDER))
                        .map(row -> new WeeklyTestScoreRow(row.getId(), row.getStudent().getId(),
                                row.getStudent().getName(), row.getStudent().getSchoolName(), row.getScore()))
                        .toList());
    }

    private WeeklyTestAttachmentItem attachment(WeeklyTestAttachment file) {
        return new WeeklyTestAttachmentItem(file.getId(), file.getOriginalName(), formatSize(file.getFileSize()));
    }

    private String averageScore(WeeklyTest test) {
        double average = test.getScores().stream()
                .map(WeeklyTestScore::getScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);
        if (Double.isNaN(average)) return null;
        if (average == Math.rint(average)) return "%d점".formatted((int) average);
        return String.format(Locale.KOREAN, "%.1f점", average);
    }

    private String testName(LocalDate date, String courseName) {
        String[] weeks = {"첫째", "둘째", "셋째", "넷째", "다섯째"};
        int index = Math.min((date.getDayOfMonth() - 1) / 7, weeks.length - 1);
        return "[%d년 %02d월 %s주 테스트] %s".formatted(
                date.getYear(), date.getMonthValue(), weeks[index], courseName);
    }

    private Integer parseScore(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            int score = Integer.parseInt(raw.trim());
            if (score < 0 || score > 100) throw new NumberFormatException();
            return score;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("점수는 0점 이상 100점 이하의 정수로 입력해 주세요.");
        }
    }

    private String normalizeRemarks(String remarks) {
        String value = remarks == null ? "" : remarks.trim();
        if (value.length() > MAX_REMARKS_LENGTH) {
            throw new IllegalArgumentException("비고는 2,000자 이하로 입력해 주세요.");
        }
        return value.isEmpty() ? null : value;
    }

    private List<MultipartFile> validateFiles(List<MultipartFile> files) {
        List<MultipartFile> actual = files == null ? List.of() : files.stream().filter(file -> !file.isEmpty()).toList();
        if (actual.size() > MAX_FILES) throw new IllegalArgumentException("첨부파일은 최대 5개까지 등록할 수 있습니다.");
        if (actual.stream().anyMatch(file -> file.getSize() > MAX_FILE_SIZE)) {
            throw new IllegalArgumentException("첨부파일은 파일당 10MB 이하여야 합니다.");
        }
        return actual;
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) throw new IllegalArgumentException("조회 기간을 입력해 주세요.");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("조회 종료일은 시작일보다 빠를 수 없습니다.");
    }

    private Teacher teacher(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private Course course(Long courseId, Teacher teacher) {
        if (courseId == null) throw new IllegalArgumentException("테스트를 등록할 반을 선택해 주세요.");
        return courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
                .filter(Course::isActive)
                .orElseThrow(() -> new IllegalArgumentException("선택한 반을 찾을 수 없습니다."));
    }

    private WeeklyTest owned(Teacher teacher, Long testId) {
        return weeklyTestRepository.findByIdAndTeacherId(testId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("주간테스트를 찾을 수 없습니다."));
    }

    private void deleteOnRollback(String storedName) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) fileStorage.deleteIfExists(storageRoot, storedName);
            }
        });
    }

    private String formatSize(long bytes) {
        if (bytes >= 1048576) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        if (bytes >= 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
}
