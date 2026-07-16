package com.naenae.teacher.dashboard.controller;

import java.time.LocalDate;

import com.naenae.common.user.domain.User;
import com.naenae.common.vocabulary.service.TodaySentenceService;
import com.naenae.common.vocabulary.service.TodayWordService;
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
    private final TodayWordService todayWordService;
    private final TodaySentenceService todaySentenceService;

    public TeacherDashboardController(
            TeacherDashboardService teacherDashboardService,
            TodayWordService todayWordService,
            TodaySentenceService todaySentenceService
    ) {
        this.teacherDashboardService = teacherDashboardService;
        this.todayWordService = todayWordService;
        this.todaySentenceService = todaySentenceService;
    }

    @GetMapping("/teacher/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        Long teacherUserId = resolveTeacherUserId(authentication);
        TeacherDashboard dashboard = teacherDashboardService.getDashboard(teacherUserId);

        model.addAttribute("totalStudentCount", dashboard.totalStudentCount());
        model.addAttribute("todayPresentCount", dashboard.todayPresentCount());
        model.addAttribute("todayLateCount", dashboard.todayLateCount());
        model.addAttribute("todayAbsentCount", dashboard.todayAbsentCount());
        model.addAttribute("todayAttendanceRate", dashboard.todayAttendanceRate());
        model.addAttribute("openAssignmentCount", dashboard.openAssignmentCount());
        model.addAttribute("recentMemoCount", dashboard.recentMemoCount());
        model.addAttribute("todayNotice", dashboard.todayNotice());
        model.addAttribute("boardPosts", dashboard.boardPosts());
        model.addAttribute("todaySchedules", dashboard.todaySchedules());
        model.addAttribute("todayEnglishWords", todayWordService.getTeacherTodayWords(LocalDate.now(), teacherUserId));
        model.addAttribute("todayEnglishSentences", todaySentenceService.getTeacherTodaySentences(LocalDate.now(), teacherUserId));
        return "teacher/dashboard";
    }

    private Long resolveTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
