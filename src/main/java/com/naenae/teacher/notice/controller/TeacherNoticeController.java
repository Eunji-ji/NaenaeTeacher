package com.naenae.teacher.notice.controller;

import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.notice.model.NoticeDownload;
import com.naenae.common.notice.model.NoticeFormData;
import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.notice.service.TeacherNoticeService;
import java.util.List;
import org.springframework.core.io.Resource;
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
public class TeacherNoticeController {
    private final TeacherNoticeService noticeService;
    private final FileDownloadResponseFactory downloadFactory;

    public TeacherNoticeController(TeacherNoticeService noticeService, FileDownloadResponseFactory downloadFactory) {
        this.noticeService = noticeService;
        this.downloadFactory = downloadFactory;
    }

    @GetMapping("/teacher/notice")
    public String list(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        model.addAttribute("noticePage", noticeService.getNotices(userId(authentication), page));
        return "teacher/notices";
    }

    @GetMapping("/teacher/notice/new")
    public String createForm(Authentication authentication, Model model) {
        populateForm(model, userId(authentication), null, "", "", true, List.of(), List.of());
        return "teacher/notice-form";
    }

    @PostMapping("/teacher/notice")
    public String create(@RequestParam String title, @RequestParam String contentHtml,
                         @RequestParam(defaultValue = "false") boolean targetAll,
                         @RequestParam(required = false) List<Long> courseIds,
                         @RequestParam(required = false) List<MultipartFile> attachments,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Long userId = userId(authentication);
        try {
            noticeService.create(userId, title, contentHtml, targetAll, courseIds, attachments);
            redirectAttributes.addFlashAttribute("successMessage", "알림장을 등록했습니다.");
            return "redirect:/teacher/notice";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            populateForm(model, userId, null, title, contentHtml, targetAll, courseIds, List.of());
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/notice-form";
        }
    }

    @GetMapping("/teacher/notice/{noticeId}")
    public String detail(@PathVariable Long noticeId, Authentication authentication, Model model) {
        model.addAttribute("notice", noticeService.getNotice(userId(authentication), noticeId));
        return "teacher/notice-detail";
    }

    @GetMapping("/teacher/notice/{noticeId}/edit")
    public String editForm(@PathVariable Long noticeId, Authentication authentication, Model model) {
        Long userId = userId(authentication);
        NoticeFormData form = noticeService.getForm(userId, noticeId);
        populateForm(model, userId, form.id(), form.title(), form.contentHtml(), form.targetAll(),
                form.selectedCourses().stream().map(course -> course.id()).toList(), form.attachments());
        return "teacher/notice-form";
    }

    @PostMapping("/teacher/notice/{noticeId}")
    public String update(@PathVariable Long noticeId, @RequestParam String title, @RequestParam String contentHtml,
                         @RequestParam(defaultValue = "false") boolean targetAll,
                         @RequestParam(required = false) List<Long> courseIds,
                         @RequestParam(required = false) List<MultipartFile> attachments,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Long userId = userId(authentication);
        try {
            noticeService.update(userId, noticeId, title, contentHtml, targetAll, courseIds, attachments);
            redirectAttributes.addFlashAttribute("successMessage", "알림장을 수정했습니다.");
            return "redirect:/teacher/notice/" + noticeId;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            NoticeFormData existing = noticeService.getForm(userId, noticeId);
            populateForm(model, userId, noticeId, title, contentHtml, targetAll, courseIds, existing.attachments());
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/notice-form";
        }
    }

    @PostMapping("/teacher/notice/{noticeId}/delete")
    public String delete(@PathVariable Long noticeId, Authentication authentication, RedirectAttributes attributes) {
        noticeService.delete(userId(authentication), noticeId);
        attributes.addFlashAttribute("successMessage", "알림장을 삭제했습니다.");
        return "redirect:/teacher/notice";
    }

    @GetMapping("/teacher/notice/{noticeId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long noticeId, @PathVariable Long attachmentId,
                                             Authentication authentication) {
        NoticeDownload download = noticeService.download(userId(authentication), noticeId, attachmentId);
        return downloadFactory.create(download.path(), download.originalName(), download.contentType());
    }

    private void populateForm(Model model, Long userId, Long noticeId, String title, String contentHtml,
                              boolean targetAll, List<Long> selectedIds, List<?> attachments) {
        List<Long> ids = selectedIds == null ? List.of() : selectedIds;
        model.addAttribute("courses", noticeService.getCourses(userId));
        model.addAttribute("selectedCourseIds", ids);
        model.addAttribute("noticeId", noticeId);
        model.addAttribute("title", title);
        model.addAttribute("contentHtml", contentHtml);
        model.addAttribute("targetAll", targetAll);
        model.addAttribute("existingAttachments", attachments);
        model.addAttribute("editMode", noticeId != null);
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
