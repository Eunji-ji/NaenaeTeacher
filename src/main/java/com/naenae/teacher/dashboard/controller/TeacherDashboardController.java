package com.naenae.teacher.dashboard.controller;

import com.naenae.teacher.dashboard.model.TeacherDashboard;
import com.naenae.teacher.dashboard.service.TeacherDashboardService;
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
    public String dashboard(Model model) {
        TeacherDashboard dashboard = teacherDashboardService.getDashboard();
        model.addAttribute("totalStudentCount", dashboard.totalStudentCount());
        model.addAttribute("todayAttendanceCount", dashboard.todayAttendanceCount());
        model.addAttribute("openAssignmentCount", dashboard.openAssignmentCount());
        model.addAttribute("recentMemoCount", dashboard.recentMemoCount());
        return "teacher/dashboard";
    }
}
