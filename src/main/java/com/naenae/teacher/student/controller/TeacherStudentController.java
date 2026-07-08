package com.naenae.teacher.student.controller;

import java.util.List;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.student.service.TeacherStudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TeacherStudentController {

    private final TeacherStudentService teacherStudentService;

    public TeacherStudentController(TeacherStudentService teacherStudentService) {
        this.teacherStudentService = teacherStudentService;
    }

    @GetMapping("/teacher/students")
    public String students(
            @RequestParam(required = false) Long courseId,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        model.addAttribute("courses", teacherStudentService.getCourseOptions(teacherUserId));
        model.addAttribute("students", teacherStudentService.getStudents(teacherUserId, courseId));
        model.addAttribute("selectedCourseId", courseId);
        return "teacher/students";
    }

    @PostMapping("/teacher/students")
    public String createStudent(
            @RequestParam String name,
            @RequestParam(required = false) List<Long> courseIds,
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String phone,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherStudentService.createStudent(teacherUserId, name, courseIds, schoolName, phone);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("courseIds", courseIds == null ? List.of() : courseIds);
            model.addAttribute("schoolName", schoolName);
            model.addAttribute("phone", phone);
            model.addAttribute("courses", teacherStudentService.getCourseOptions(teacherUserId));
            model.addAttribute("students", teacherStudentService.getStudents(teacherUserId, null));
            return "teacher/students";
        }
        return "redirect:/teacher/students";
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}