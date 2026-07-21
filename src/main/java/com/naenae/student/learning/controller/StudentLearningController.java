package com.naenae.student.learning.controller;

import com.naenae.common.user.domain.User;
import com.naenae.student.learning.service.StudentLearningService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentLearningController {

    private final StudentLearningService service;

    public StudentLearningController(StudentLearningService service) {
        this.service = service;
    }

    @GetMapping("/student/learning")
    public String learning() {
        return "redirect:/student/learning/scores";
    }

    @GetMapping("/student/learning/scores")
    public String scores(Authentication authentication, Model model) {
        model.addAttribute("scorePage", service.getScores(userId(authentication)));
        return "student/learning-scores";
    }

    @GetMapping("/student/learning/word-tests")
    public String wordTests(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("wordTestPage", service.getWordTests(userId(authentication), page));
        return "student/learning-word-tests";
    }

    @GetMapping("/student/learning/attendance")
    public String attendance(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("attendancePage", service.getAttendance(userId(authentication), page));
        return "student/learning-attendance";
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
