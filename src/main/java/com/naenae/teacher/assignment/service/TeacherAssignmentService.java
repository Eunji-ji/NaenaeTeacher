package com.naenae.teacher.assignment.service;

import com.naenae.common.file.LocalFileStorage;
import com.naenae.common.file.StoredFile;
import com.naenae.common.pagination.PageView;
import com.naenae.common.pagination.PaginationSupport;
import com.naenae.teacher.assignment.domain.Assignment;
import com.naenae.teacher.assignment.domain.AssignmentAttachment;
import com.naenae.teacher.assignment.model.AssignmentAttachmentItem;
import com.naenae.teacher.assignment.model.AssignmentDetail;
import com.naenae.teacher.assignment.model.AssignmentDownload;
import com.naenae.teacher.assignment.model.AssignmentFormData;
import com.naenae.teacher.assignment.model.AssignmentListItem;
import com.naenae.teacher.assignment.repository.AssignmentRepository;
import com.naenae.teacher.course.domain.Course;
import com.naenae.teacher.course.repository.CourseRepository;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import com.naenae.teacher.student.model.CourseOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
public class TeacherAssignmentService {

    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final LocalFileStorage fileStorage;
    private final Path assignmentStorageRoot;

    public TeacherAssignmentService(
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            LocalFileStorage fileStorage,
            @Value("${app.storage.assignment-dir}") String assignmentStorageDir
    ) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.fileStorage = fileStorage;
        this.assignmentStorageRoot = Path.of(assignmentStorageDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageView<AssignmentListItem> getAssignments(Long teacherUserId, int page) {
        Teacher teacher = getTeacher(teacherUserId);
        return PaginationSupport.toView(
                assignmentRepository.findByTeacherIdOrderByCreatedAtDescIdDesc(
                        teacher.getId(), PaginationSupport.pageRequest(page)
                ).map(this::toListItem)
        );
    }

    @Transactional(readOnly = true)
    public AssignmentDetail getAssignment(Long teacherUserId, Long assignmentId) {
        Assignment assignment = getOwnedAssignment(getTeacher(teacherUserId), assignmentId);
        return new AssignmentDetail(
                assignment.getId(),
                assignment.getCreatedAt(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                courseNames(assignment),
                assignment.getTitle(),
                assignment.getContentHtml(),
                attachmentItems(assignment)
        );
    }

    @Transactional(readOnly = true)
    public AssignmentFormData getAssignmentForm(Long teacherUserId, Long assignmentId) {
        Assignment assignment = getOwnedAssignment(getTeacher(teacherUserId), assignmentId);
        return new AssignmentFormData(
                assignment.getId(),
                assignment.getCourses().stream()
                        .map(mapping -> new CourseOption(mapping.getCourse().getId(), mapping.getCourse().getTitle()))
                        .sorted(Comparator.comparing(CourseOption::title, String.CASE_INSENSITIVE_ORDER))
                        .toList(),
                assignment.getTitle(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                assignment.getContentHtml(),
                attachmentItems(assignment)
        );
    }

    @Transactional(readOnly = true)
    public AssignmentDownload getAttachmentDownload(Long teacherUserId, Long assignmentId, Long attachmentId) {
        Assignment assignment = getOwnedAssignment(getTeacher(teacherUserId), assignmentId);
        AssignmentAttachment attachment = assignment.getAttachments().stream()
                .filter(item -> item.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
        Path file = fileStorage.resolveExisting(assignmentStorageRoot, attachment.getStoredName());
        return new AssignmentDownload(file, attachment.getOriginalName(), attachment.getContentType());
    }

    @Transactional
    public void create(
            Long teacherUserId,
            List<Long> courseIds,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionHtml,
            List<MultipartFile> files
    ) {
        Teacher teacher = getTeacher(teacherUserId);
        List<Course> courses = getCourses(teacher, courseIds);
        AssignmentValues values = validateValues(title, startDate, endDate, descriptionHtml);
        List<MultipartFile> actualFiles = validateFiles(files, 0);

        Assignment assignment = Assignment.create(
                teacher, values.title(), values.contentHtml(), values.startDate(), values.endDate()
        );
        courses.forEach(assignment::addCourse);
        saveAttachments(assignment, actualFiles);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void update(
            Long teacherUserId,
            Long assignmentId,
            List<Long> courseIds,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionHtml,
            List<MultipartFile> files
    ) {
        Teacher teacher = getTeacher(teacherUserId);
        Assignment assignment = getOwnedAssignment(teacher, assignmentId);
        List<Course> courses = getCourses(teacher, courseIds);
        AssignmentValues values = validateValues(title, startDate, endDate, descriptionHtml);
        List<MultipartFile> actualFiles = validateFiles(files, assignment.getAttachments().size());

        assignment.update(values.title(), values.contentHtml(), values.startDate(), values.endDate());
        assignment.replaceCourses(courses);
        saveAttachments(assignment, actualFiles);
    }

    @Transactional
    public void delete(Long teacherUserId, Long assignmentId) {
        Assignment assignment = getOwnedAssignment(getTeacher(teacherUserId), assignmentId);
        List<String> storedNames = assignment.getAttachments().stream()
                .map(AssignmentAttachment::getStoredName)
                .toList();
        assignmentRepository.delete(assignment);
        deleteFilesAfterCommit(storedNames);
    }

    private AssignmentListItem toListItem(Assignment assignment) {
        return new AssignmentListItem(
                assignment.getId(),
                assignment.getCreatedAt(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                courseNames(assignment),
                assignment.getTitle(),
                assignment.getAttachments().size()
        );
    }

    private List<AssignmentAttachmentItem> attachmentItems(Assignment assignment) {
        return assignment.getAttachments().stream()
                .sorted(Comparator.comparing(AssignmentAttachment::getId))
                .map(attachment -> new AssignmentAttachmentItem(
                        attachment.getId(),
                        attachment.getOriginalName(),
                        formatFileSize(attachment.getFileSize())
                ))
                .toList();
    }

    private void saveAttachments(Assignment assignment, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            StoredFile storedFile = fileStorage.store(assignmentStorageRoot, file);
            assignment.addAttachment(
                    storedFile.originalName(),
                    storedFile.storedName(),
                    storedFile.contentType(),
                    storedFile.size()
            );
        }
    }

    private AssignmentValues validateValues(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String descriptionHtml
    ) {
        String cleanTitle = require(title, "과제 제목을 입력해 주세요.");
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("과제 게시 기간을 확인해 주세요.");
        }
        String cleanHtml = Jsoup.clean(
                descriptionHtml == null ? "" : descriptionHtml,
                Safelist.relaxed().removeTags("img")
        );
        return new AssignmentValues(cleanTitle, startDate, endDate, cleanHtml);
    }

    private List<MultipartFile> validateFiles(List<MultipartFile> files, int existingCount) {
        List<MultipartFile> actualFiles = files == null
                ? List.of()
                : files.stream().filter(file -> !file.isEmpty()).toList();
        if (existingCount + actualFiles.size() > MAX_FILES) {
            throw new IllegalArgumentException("첨부파일은 기존 파일을 포함해 최대 5개까지 등록할 수 있습니다.");
        }
        actualFiles.forEach(file -> {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("첨부파일은 파일당 10MB 이하여야 합니다.");
            }
        });
        return actualFiles;
    }

    private void deleteFilesAfterCommit(List<String> storedNames) {
        Runnable cleanup = () -> storedNames.forEach(
                storedName -> fileStorage.deleteIfExists(assignmentStorageRoot, storedName)
        );
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

    private String courseNames(Assignment assignment) {
        return assignment.getCourses().stream()
                .map(mapping -> mapping.getCourse().getTitle())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    private Assignment getOwnedAssignment(Teacher teacher, Long assignmentId) {
        return assignmentRepository.findByIdAndTeacherId(assignmentId, teacher.getId())
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));
    }

    private Teacher getTeacher(Long teacherUserId) {
        return teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new IllegalStateException("선생님 정보를 찾을 수 없습니다."));
    }

    private List<Course> getCourses(Teacher teacher, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("과제를 등록할 반을 선택해 주세요.");
        }
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(courseIds));
        List<Course> courses = courseRepository.findByTeacherIdAndIdInOrderByTitleAsc(teacher.getId(), distinctIds);
        if (courses.size() != distinctIds.size()) {
            throw new IllegalArgumentException("선택한 반 정보를 확인할 수 없습니다.");
        }
        return courses;
    }

    private String require(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String formatFileSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f MB", bytes / 1024.0 / 1024.0);
        }
        if (bytes >= 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

    private record AssignmentValues(String title, LocalDate startDate, LocalDate endDate, String contentHtml) {
    }
}