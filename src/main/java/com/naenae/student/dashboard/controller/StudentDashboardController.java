package com.naenae.student.dashboard.controller;

import com.naenae.common.user.domain.User;
import com.naenae.student.dashboard.model.StudentDashboard;
import com.naenae.student.dashboard.service.StudentDashboardService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    public StudentDashboardController(StudentDashboardService studentDashboardService) {
        this.studentDashboardService = studentDashboardService;
    }

    @GetMapping("/student")
    public String studentRoot() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Long userId = resolveUserId(authentication);
        StudentDashboard dashboard = studentDashboardService.getDashboard(userId);
        model.addAttribute("studentDashboard", dashboard);
        return "student/dashboard";
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
