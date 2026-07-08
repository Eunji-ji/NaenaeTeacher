package com.naenae.teacher.attendance.controller;

import java.time.LocalDate;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.attendance.domain.AttendanceStatus;
import com.naenae.teacher.attendance.service.TeacherAttendanceService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    public TeacherAttendanceController(TeacherAttendanceService teacherAttendanceService) {
        this.teacherAttendanceService = teacherAttendanceService;
    }

    @GetMapping("/teacher/attendance")
    public String attendance(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer shift,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        LocalDate resolvedDate = resolveDate(date, shift);
        model.addAttribute("attendancePage", teacherAttendanceService.getPage(teacherUserId, courseId, resolvedDate));
        return "teacher/attendance";
    }

    @PostMapping("/teacher/attendance")
    public String saveAttendance(
            @RequestParam(required = false) Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long studentId,
            @RequestParam(required = false) AttendanceStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherAttendanceService.saveAttendance(teacherUserId, courseId, date, studentId, status);
            redirectAttributes.addFlashAttribute("successMessage", "출결을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        String redirect = "redirect:/teacher/attendance?date=" + date;
        if (courseId != null) {
            redirect += "&courseId=" + courseId;
        }
        return redirect;
    }

    private LocalDate resolveDate(LocalDate date, Integer shift) {
        LocalDate baseDate = date == null ? LocalDate.now() : date;
        if (shift != null) {
            return baseDate.plusDays(shift);
        }
        return baseDate;
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
