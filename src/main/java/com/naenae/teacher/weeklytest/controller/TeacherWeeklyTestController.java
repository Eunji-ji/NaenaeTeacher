package com.naenae.teacher.weeklytest.controller;

import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.weeklytest.model.WeeklyTestDownload;
import com.naenae.teacher.weeklytest.service.TeacherWeeklyTestService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class TeacherWeeklyTestController {

    private final TeacherWeeklyTestService service;
    private final FileDownloadResponseFactory downloadFactory;

    public TeacherWeeklyTestController(TeacherWeeklyTestService service, FileDownloadResponseFactory downloadFactory) {
        this.service = service;
        this.downloadFactory = downloadFactory;
    }

    @GetMapping("/teacher/weekly-tests")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model
    ) {
        LocalDate actualEnd = endDate == null ? LocalDate.now() : endDate;
        LocalDate actualStart = startDate == null ? actualEnd.minusMonths(2) : startDate;
        Long userId = userId(authentication);
        try {
            model.addAttribute("weeklyTestPage", service.getTests(userId, actualStart, actualEnd, page));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            actualStart = LocalDate.now().minusMonths(2);
            actualEnd = LocalDate.now();
            model.addAttribute("weeklyTestPage", service.getTests(userId, actualStart, actualEnd, 0));
        }
        model.addAttribute("startDate", actualStart);
        model.addAttribute("endDate", actualEnd);
        return "teacher/weekly-tests";
    }

    @GetMapping("/teacher/weekly-tests/new")
    public String form(Authentication authentication, Model model) {
        model.addAttribute("courses", service.getCourses(userId(authentication)));
        return "teacher/weekly-test-form";
    }

    @PostMapping("/teacher/weekly-tests")
    public String create(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) List<MultipartFile> attachments,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = userId(authentication);
        try {
            Long id = service.create(userId, courseId, remarks, attachments);
            redirectAttributes.addFlashAttribute("successMessage", "이번 주 테스트를 등록했습니다.");
            return "redirect:/teacher/weekly-tests/" + id;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("courses", service.getCourses(userId));
            model.addAttribute("courseId", courseId);
            model.addAttribute("remarks", remarks);
            return "teacher/weekly-test-form";
        }
    }

    @GetMapping("/teacher/weekly-tests/{testId}")
    public String detail(@PathVariable Long testId, Authentication authentication, Model model) {
        model.addAttribute("weeklyTest", service.getDetail(userId(authentication), testId));
        return "teacher/weekly-test-detail";
    }

    @PostMapping("/teacher/weekly-tests/{testId}/scores")
    public String scores(@PathVariable Long testId, @RequestParam Map<String, String> parameters,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        Map<Long, String> scores = new LinkedHashMap<>();
        parameters.forEach((key, value) -> {
            if (key.startsWith("score_")) {
                try { scores.put(Long.parseLong(key.substring(6)), value); }
                catch (NumberFormatException ignored) { }
            }
        });
        try {
            service.updateScores(userId(authentication), testId, scores);
            redirectAttributes.addFlashAttribute("successMessage", "학생 점수를 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/teacher/weekly-tests/" + testId;
    }

    @GetMapping("/teacher/weekly-tests/{testId}/attachments/{attachmentId}/download")
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
