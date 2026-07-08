package com.naenae.teacher.dashboard.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.dashboard.model.TeacherDashboard;
import com.naenae.teacher.dashboard.service.TeacherDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    public TeacherDashboardController(TeacherDashboardService teacherDashboardService) {
        this.teacherDashboardService = teacherDashboardService;
    }

    @GetMapping("/teacher/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        TeacherDashboard dashboard = teacherDashboardService.getDashboard();
        String teacherName = resolveTeacherName(authentication);
        model.addAttribute("totalStudentCount", dashboard.totalStudentCount());
        model.addAttribute("todayAttendanceCount", dashboard.todayAttendanceCount());
        model.addAttribute("openAssignmentCount", dashboard.openAssignmentCount());
        model.addAttribute("recentMemoCount", dashboard.recentMemoCount());
        model.addAttribute("teacherName", teacherName);
        model.addAttribute("teacherInitial", teacherName.substring(0, 1));
        return "teacher/dashboard";
    }

    private String resolveTeacherName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            if (user.getName() != null && !user.getName().isBlank()) {
                return user.getName();
            }
        }
        return "선생님";
    }
}
