package com.naenae.student.notice.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.notice.domain.Notice;
import com.naenae.common.notice.domain.NoticeAttachment;
import com.naenae.common.notice.model.*;
import com.naenae.common.notice.repository.NoticeRepository;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.student.profile.domain.Student;
import com.naenae.student.profile.repository.StudentRepository;
import com.naenae.teacher.course.repository.CourseStudentRepository;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentNoticeService {
    private final StudentRepository studentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final NoticeRepository noticeRepository;
    private final LocalFileStorage fileStorage;
    private final Path storageRoot;

    public StudentNoticeService(StudentRepository studentRepository, CourseStudentRepository courseStudentRepository,
                                NoticeRepository noticeRepository, LocalFileStorage fileStorage,
                                @Value("${app.storage.notice-dir}") String storageDir) {
        this.studentRepository = studentRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.noticeRepository = noticeRepository;
        this.fileStorage = fileStorage;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageView<NoticeListItem> getNotices(Long userId, int page) {
        Student student = student(userId);
        return PaginationSupport.toView(noticeRepository.findVisibleToStudent(
                student.getTeacher().getId(), queryCourseIds(student), PaginationSupport.pageRequest(page)).map(this::toListItem));
    }

    @Transactional(readOnly = true)
    public List<NoticeListItem> getRecentNotices(Student student, int size) {
        return noticeRepository.findVisibleToStudent(student.getTeacher().getId(), queryCourseIds(student), PageRequest.of(0, size))
                .map(this::toListItem).getContent();
    }

    @Transactional(readOnly = true)
    public NoticeDetail getNotice(Long userId, Long noticeId) {
        Notice notice = visible(student(userId), noticeId);
        return new NoticeDetail(notice.getId(), notice.getCreatedAt(), notice.getTitle(), targetLabel(notice),
                notice.getContentHtml(), attachments(notice));
    }

    @Transactional(readOnly = true)
    public NoticeDownload download(Long userId, Long noticeId, Long attachmentId) {
        Notice notice = visible(student(userId), noticeId);
        NoticeAttachment attachment = notice.getAttachments().stream().filter(item -> item.getId().equals(attachmentId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        return new NoticeDownload(fileStorage.resolveExisting(storageRoot, attachment.getStoredName()),
                attachment.getOriginalName(), attachment.getContentType());
    }

    private Notice visible(Student student, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndTeacherId(noticeId, student.getTeacher().getId())
                .orElseThrow(() -> new IllegalArgumentException("알림장을 찾을 수 없습니다."));
        if (notice.isTargetAll()) return notice;
        Set<Long> studentCourseIds = new HashSet<>(courseIds(student));
        if (notice.getCourses().stream().noneMatch(mapping -> studentCourseIds.contains(mapping.getCourse().getId())))
            throw new IllegalArgumentException("알림장을 찾을 수 없습니다.");
        return notice;
    }

    private List<Long> queryCourseIds(Student student) {
        List<Long> ids = courseIds(student);
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    private List<Long> courseIds(Student student) {
        return courseStudentRepository.findByStudent_IdOrderByCourseTitleAsc(student.getId()).stream()
                .map(mapping -> mapping.getCourse().getId()).distinct().toList();
    }

    private NoticeListItem toListItem(Notice notice) {
        return new NoticeListItem(notice.getId(), notice.getCreatedAt(), notice.getTitle(), targetLabel(notice), notice.getAttachments().size());
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

    private Student student(Long userId) {
        return studentRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
    }

    private String size(long bytes) {
        if (bytes >= 1048576) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        if (bytes >= 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
}