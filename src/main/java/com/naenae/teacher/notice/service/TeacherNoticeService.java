package com.naenae.teacher.notice.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.notice.domain.Notice;
import com.naenae.common.notice.domain.NoticeAttachment;
import com.naenae.common.notice.model.*;
import com.naenae.common.notice.repository.NoticeRepository;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TeacherNoticeService {
    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final NoticeRepository noticeRepository;
    private final LocalFileStorage fileStorage;
    private final Path storageRoot;

    public TeacherNoticeService(TeacherRepository teacherRepository, CourseRepository courseRepository,
                                NoticeRepository noticeRepository, LocalFileStorage fileStorage,
                                @Value("${app.storage.notice-dir}") String storageDir) {
        this.teacherRepository = teacherRepository; this.courseRepository = courseRepository;
        this.noticeRepository = noticeRepository; this.fileStorage = fileStorage;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<CourseOption> getCourses(Long userId) {
        Teacher teacher = getTeacher(userId);
        return courseRepository.findByTeacherIdOrderByTitleAsc(teacher.getId()).stream()
                .map(course -> new CourseOption(course.getId(), course.getTitle())).toList();
    }

    @Transactional(readOnly = true)
    public PageView<NoticeListItem> getNotices(Long userId, int page) {
        Teacher teacher = getTeacher(userId);
        return PaginationSupport.toView(noticeRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                teacher.getId(), PaginationSupport.pageRequest(page)).map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public Optional<DashboardNoticeItem> getDashboardNotice(Long userId) {
        Teacher teacher = getTeacher(userId);
        LocalDate today = LocalDate.now();
        return noticeRepository
                .findFirstByTeacherIdAndPublishStartDateLessThanEqualAndPublishEndDateGreaterThanEqualOrderByCreatedAtDescIdDesc(
                        teacher.getId(), today, today)
                .map(this::toDashboardItem);
    }

    @Transactional(readOnly = true)
    public NoticeDetail getNotice(Long userId, Long noticeId) {
        Notice notice = owned(getTeacher(userId), noticeId);
        return new NoticeDetail(notice.getId(), notice.getCreatedAt(), notice.getTitle(), targetLabel(notice),
                notice.getContentHtml(), attachments(notice));
    }

    @Transactional(readOnly = true)
    public NoticeFormData getForm(Long userId, Long noticeId) {
        Notice notice = owned(getTeacher(userId), noticeId);
        return new NoticeFormData(notice.getId(), notice.getTitle(), notice.getContentHtml(), notice.isTargetAll(),
                notice.getPublishStartDate(), notice.getPublishEndDate(),
                notice.getCourses().stream().map(mapping -> new CourseOption(mapping.getCourse().getId(), mapping.getCourse().getTitle()))
                        .sorted(Comparator.comparing(CourseOption::title, String.CASE_INSENSITIVE_ORDER)).toList(), attachments(notice));
    }

    @Transactional(readOnly = true)
    public NoticeDownload download(Long userId, Long noticeId, Long attachmentId) {
        Notice notice = owned(getTeacher(userId), noticeId);
        NoticeAttachment attachment = notice.getAttachments().stream().filter(item -> item.getId().equals(attachmentId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        return new NoticeDownload(fileStorage.resolveExisting(storageRoot, attachment.getStoredName()),
                attachment.getOriginalName(), attachment.getContentType());
    }

    @Transactional
    public void create(Long userId, String title, String html, boolean targetAll,
                       LocalDate publishStartDate, LocalDate publishEndDate,
                       List<Long> courseIds, List<MultipartFile> files) {
        Teacher teacher = getTeacher(userId); Values values = values(title, html);
        validatePeriod(publishStartDate, publishEndDate);
        List<Course> courses = targetCourses(teacher, targetAll, courseIds); List<MultipartFile> actual = files(files, 0);
        Notice notice = Notice.create(teacher, values.title(), values.html(), targetAll, publishStartDate, publishEndDate);
        courses.forEach(notice::addCourse); saveFiles(notice, actual); noticeRepository.save(notice);
    }

    @Transactional
    public void update(Long userId, Long noticeId, String title, String html, boolean targetAll,
                       LocalDate publishStartDate, LocalDate publishEndDate,
                       List<Long> courseIds, List<MultipartFile> files) {
        Teacher teacher = getTeacher(userId); Notice notice = owned(teacher, noticeId); Values values = values(title, html);
        validatePeriod(publishStartDate, publishEndDate);
        List<Course> courses = targetCourses(teacher, targetAll, courseIds);
        List<MultipartFile> actual = files(files, notice.getAttachments().size());
        notice.update(values.title(), values.html(), targetAll, publishStartDate, publishEndDate);
        notice.replaceCourses(courses); saveFiles(notice, actual);
    }

    @Transactional
    public void delete(Long userId, Long noticeId) {
        Notice notice = owned(getTeacher(userId), noticeId);
        List<String> names = notice.getAttachments().stream().map(NoticeAttachment::getStoredName).toList();
        noticeRepository.delete(notice); deleteAfterCommit(names);
    }

    private NoticeListItem toListItem(Notice notice) {
        return new NoticeListItem(notice.getId(), notice.getCreatedAt(), notice.getTitle(), targetLabel(notice), notice.getAttachments().size());
    }
    private DashboardNoticeItem toDashboardItem(Notice notice) {
        String text = Jsoup.parse(notice.getContentHtml()).text();
        String summary = text.length() > 140 ? text.substring(0, 140) + "…" : text;
        return new DashboardNoticeItem(notice.getId(), notice.getCreatedAt(), notice.getTitle(),
                targetLabel(notice), summary);
    }
    private List<NoticeAttachmentItem> attachments(Notice notice) {
        return notice.getAttachments().stream().sorted(Comparator.comparing(NoticeAttachment::getId))
                .map(item -> new NoticeAttachmentItem(item.getId(), item.getOriginalName(), size(item.getFileSize()))).toList();
    }
    private String targetLabel(Notice notice) {
        if (notice.isTargetAll()) return "전체";
        return notice.getCourses().stream().map(mapping -> mapping.getCourse().getTitle())
                .sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", "));
    }
    private List<Course> targetCourses(Teacher teacher, boolean targetAll, List<Long> ids) {
        if (targetAll) return List.of();
        if (ids == null || ids.isEmpty()) throw new IllegalArgumentException("전체 또는 알림을 보낼 반을 선택해 주세요.");
        List<Long> distinct = new ArrayList<>(new LinkedHashSet<>(ids));
        List<Course> courses = courseRepository.findByTeacherIdAndIdInOrderByTitleAsc(teacher.getId(), distinct);
        if (courses.size() != distinct.size()) throw new IllegalArgumentException("선택한 반 정보를 확인할 수 없습니다.");
        return courses;
    }
    private Values values(String title, String html) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("알림장 제목을 입력해 주세요.");
        String clean = Jsoup.clean(html == null ? "" : html, Safelist.relaxed().removeTags("img"));
        return new Values(title.trim(), clean);
    }
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) throw new IllegalArgumentException("게시기간을 선택해 주세요.");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("게시 종료일은 시작일보다 빠를 수 없습니다.");
    }
    private List<MultipartFile> files(List<MultipartFile> files, int existing) {
        List<MultipartFile> actual = files == null ? List.of() : files.stream().filter(file -> !file.isEmpty()).toList();
        if (existing + actual.size() > MAX_FILES) throw new IllegalArgumentException("첨부파일은 기존 파일을 포함해 최대 5개까지 등록할 수 있습니다.");
        actual.forEach(file -> { if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("첨부파일은 파일당 10MB 이하여야 합니다."); });
        return actual;
    }
    private void saveFiles(Notice notice, List<MultipartFile> files) {
        files.forEach(file -> { StoredFile stored = fileStorage.store(storageRoot, file);
            notice.addAttachment(stored.originalName(), stored.storedName(), stored.contentType(), stored.size()); });
    }
    private void deleteAfterCommit(List<String> names) {
        Runnable cleanup = () -> names.forEach(name -> fileStorage.deleteIfExists(storageRoot, name));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { cleanup.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { @Override public void afterCommit() { cleanup.run(); } });
    }
    private Notice owned(Teacher teacher, Long id) { return noticeRepository.findByIdAndTeacherId(id, teacher.getId())
            .orElseThrow(() -> new IllegalArgumentException("알림장을 찾을 수 없습니다.")); }
    private Teacher getTeacher(Long userId) { return teacherRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다.")); }
    private String size(long bytes) { if (bytes >= 1048576) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        if (bytes >= 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0); return bytes + " B"; }
    private record Values(String title, String html) {}
}
