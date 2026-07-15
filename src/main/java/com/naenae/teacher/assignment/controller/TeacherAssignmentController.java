package com.naenae.teacher.assignment.controller;

import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.user.domain.User;
import com.naenae.teacher.assignment.domain.AssignmentStatus;
import com.naenae.teacher.assignment.model.AssignmentDownload;
import com.naenae.teacher.assignment.model.AssignmentFormData;
import com.naenae.teacher.assignment.service.TeacherAssignmentService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.course.service.TeacherCourseService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherAssignmentController {

    private final TeacherCourseService teacherCourseService;
    private final TeacherAssignmentService teacherAssignmentService;
    private final FileDownloadResponseFactory fileDownloadResponseFactory;

    public TeacherAssignmentController(
            TeacherCourseService teacherCourseService,
            TeacherAssignmentService teacherAssignmentService,
            FileDownloadResponseFactory fileDownloadResponseFactory
    ) {
        this.teacherCourseService = teacherCourseService;
        this.teacherAssignmentService = teacherAssignmentService;
        this.fileDownloadResponseFactory = fileDownloadResponseFactory;
    }

    @GetMapping("/teacher/assignments")
    public String assignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "false") boolean inProgressOnly,
            Authentication authentication,
            Model model
    ) {
        populateListModel(getTeacherUserId(authentication), page, inProgressOnly, model);
        return "teacher/assignments";
    }

    @GetMapping("/teacher/assignments/{assignmentId}")
    public String assignmentDetail(
            @PathVariable Long assignmentId,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute(
                "assignment",
                teacherAssignmentService.getAssignment(getTeacherUserId(authentication), assignmentId)
        );
        return "teacher/assignment-detail";
    }

    @GetMapping("/teacher/assignments/{assignmentId}/edit")
    public String editAssignmentForm(
            @PathVariable Long assignmentId,
            Authentication authentication,
            Model model
    ) {
        AssignmentFormData form = teacherAssignmentService.getAssignmentForm(
                getTeacherUserId(authentication), assignmentId
        );
        populateFormModel(model, form);
        return "teacher/assignment-form";
    }

    @GetMapping("/teacher/assignments/{assignmentId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long assignmentId,
            @PathVariable Long attachmentId,
            Authentication authentication
    ) {
        AssignmentDownload download = teacherAssignmentService.getAttachmentDownload(
                getTeacherUserId(authentication), assignmentId, attachmentId
        );
        return fileDownloadResponseFactory.create(
                download.path(), download.originalName(), download.contentType()
        );
    }

    @PostMapping("/teacher/assignments")
    public String saveAssignment(
            @RequestParam List<Long> courseIds,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String descriptionHtml,
            @RequestParam AssignmentStatus status,
            @RequestParam(required = false) List<MultipartFile> attachments,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherAssignmentService.create(
                    teacherUserId, courseIds, title, startDate, endDate, descriptionHtml, status, attachments
            );
            redirectAttributes.addFlashAttribute("successMessage", "과제를 저장했습니다.");
            return "redirect:/teacher/assignments";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            populateSubmittedForm(
                    model, teacherUserId, null, courseIds, title, startDate, endDate, status, descriptionHtml, List.of()
            );
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/assignment-form";
        }
    }

    @PostMapping("/teacher/assignments/{assignmentId}")
    public String updateAssignment(
            @PathVariable Long assignmentId,
            @RequestParam List<Long> courseIds,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String descriptionHtml,
            @RequestParam AssignmentStatus status,
            @RequestParam(required = false) List<MultipartFile> attachments,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherAssignmentService.update(
                    teacherUserId,
                    assignmentId,
                    courseIds,
                    title,
                    startDate,
                    endDate,
                    descriptionHtml,
                    status,
                    attachments
            );
            redirectAttributes.addFlashAttribute("successMessage", "과제를 수정했습니다.");
            return "redirect:/teacher/assignments";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            AssignmentFormData existing = teacherAssignmentService.getAssignmentForm(teacherUserId, assignmentId);
            populateSubmittedForm(
                    model,
                    teacherUserId,
                    assignmentId,
                    courseIds,
                    title,
                    startDate,
                    endDate,
                    status,
                    descriptionHtml,
                    existing.attachments()
            );
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/assignment-form";
        }
    }

    @PostMapping("/teacher/assignments/{assignmentId}/delete")
    public String deleteAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        teacherAssignmentService.delete(getTeacherUserId(authentication), assignmentId);
        redirectAttributes.addFlashAttribute("successMessage", "과제를 삭제했습니다.");
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/teacher/assignments/new")
    public String assignmentForm(
            @RequestParam(required = false) List<Long> courseIds,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        if (courseIds == null || courseIds.isEmpty()) {
            model.addAttribute("errorMessage", "과제를 등록할 반을 1개 이상 선택해 주세요.");
            populateListModel(teacherUserId, 0, false, model);
            return "teacher/assignments";
        }
        var selectedCourses = selectedCourses(teacherUserId, courseIds);
        if (selectedCourses.size() != courseIds.stream().distinct().count()) {
            model.addAttribute("errorMessage", "선택한 반 정보를 확인할 수 없습니다.");
            populateListModel(teacherUserId, 0, false, model);
            return "teacher/assignments";
        }
        model.addAttribute("selectedCourses", selectedCourses);
        model.addAttribute("startDate", LocalDate.now());
        model.addAttribute("endDate", LocalDate.now().plusDays(7));
        model.addAttribute("status", AssignmentStatus.IN_PROGRESS);
        model.addAttribute("existingAttachments", List.of());
        model.addAttribute("editMode", false);
        return "teacher/assignment-form";
    }

    private void populateFormModel(Model model, AssignmentFormData form) {
        model.addAttribute("selectedCourses", form.courses());
        model.addAttribute("title", form.title());
        model.addAttribute("startDate", form.startDate());
        model.addAttribute("endDate", form.endDate());
        model.addAttribute("status", form.status());
        model.addAttribute("descriptionHtml", form.contentHtml());
        model.addAttribute("existingAttachments", form.attachments());
        model.addAttribute("assignmentId", form.id());
        model.addAttribute("editMode", true);
    }

    private void populateSubmittedForm(
            Model model,
            Long teacherUserId,
            Long assignmentId,
            List<Long> courseIds,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            AssignmentStatus status,
            String descriptionHtml,
            List<?> existingAttachments
    ) {
        model.addAttribute("selectedCourses", selectedCourses(teacherUserId, courseIds));
        model.addAttribute("title", title);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("descriptionHtml", descriptionHtml);
        model.addAttribute("existingAttachments", existingAttachments);
        model.addAttribute("assignmentId", assignmentId);
        model.addAttribute("editMode", assignmentId != null);
    }

    private List<?> selectedCourses(Long teacherUserId, List<Long> courseIds) {
        return teacherCourseService.getCourses(teacherUserId).stream()
                .filter(course -> courseIds.contains(course.id()))
                .toList();
    }

    private void populateListModel(Long teacherUserId, int page, boolean inProgressOnly, Model model) {
        model.addAttribute("courses", teacherCourseService.getCourses(teacherUserId));
        model.addAttribute("inProgressOnly", inProgressOnly);
        model.addAttribute("assignmentPaginationUrl", inProgressOnly ? "/teacher/assignments?inProgressOnly=true" : "/teacher/assignments");
        model.addAttribute("assignmentPage", teacherAssignmentService.getAssignments(teacherUserId, page, inProgressOnly));
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}