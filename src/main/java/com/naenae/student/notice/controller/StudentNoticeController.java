package com.naenae.student.notice.controller;

import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.notice.model.NoticeDownload;
import com.naenae.common.user.domain.User;
import com.naenae.student.notice.service.StudentNoticeService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StudentNoticeController {
    private final StudentNoticeService noticeService;
    private final FileDownloadResponseFactory downloadFactory;

    public StudentNoticeController(StudentNoticeService noticeService, FileDownloadResponseFactory downloadFactory) {
        this.noticeService = noticeService;
        this.downloadFactory = downloadFactory;
    }

    @GetMapping("/student/notices")
    public String list(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        model.addAttribute("noticePage", noticeService.getNotices(userId(authentication), page));
        return "student/notices";
    }

    @GetMapping("/student/notices/{noticeId}")
    public String detail(@PathVariable Long noticeId, Authentication authentication, Model model) {
        model.addAttribute("notice", noticeService.getNotice(userId(authentication), noticeId));
        return "student/notice-detail";
    }

    @GetMapping("/student/notices/{noticeId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long noticeId, @PathVariable Long attachmentId,
                                             Authentication authentication) {
        NoticeDownload download = noticeService.download(userId(authentication), noticeId, attachmentId);
        return downloadFactory.create(download.path(), download.originalName(), download.contentType());
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}