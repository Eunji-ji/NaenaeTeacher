package com.naenae.student.weeklytest.controller;

import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.user.domain.User;
import com.naenae.student.weeklytest.service.StudentWeeklyTestService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.weeklytest.model.WeeklyTestDownload;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentWeeklyTestController {

    private final StudentWeeklyTestService service;
    private final FileDownloadResponseFactory downloadFactory;

    public StudentWeeklyTestController(StudentWeeklyTestService service, FileDownloadResponseFactory downloadFactory) {
        this.service = service;
        this.downloadFactory = downloadFactory;
    }

    @GetMapping("/student/weekly-tests")
    public String list(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        model.addAttribute("weeklyTestPage", service.getTests(userId(authentication), page));
        return "student/weekly-tests";
    }

    @GetMapping("/student/weekly-tests/{testId}")
    public String detail(@PathVariable Long testId, Authentication authentication, Model model) {
        model.addAttribute("weeklyTest", service.getDetail(userId(authentication), testId));
        return "student/weekly-test-detail";
    }

    @GetMapping("/student/weekly-tests/{testId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long testId, @PathVariable Long attachmentId,
                                             Authentication authentication) {
        WeeklyTestDownload file = service.download(userId(authentication), testId, attachmentId);
        return downloadFactory.create(file.path(), file.originalName(), file.contentType());
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
