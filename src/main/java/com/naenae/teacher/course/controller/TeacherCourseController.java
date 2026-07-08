package com.naenae.teacher.course.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.course.service.TeacherCourseService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    public TeacherCourseController(TeacherCourseService teacherCourseService) {
        this.teacherCourseService = teacherCourseService;
    }

    @GetMapping("/teacher/courses")
    public String courses(Authentication authentication, Model model) {
        Long teacherUserId = getTeacherUserId(authentication);
        model.addAttribute("courses", teacherCourseService.getCourses(teacherUserId));
        return "teacher/courses";
    }

    @PostMapping("/teacher/courses")
    public String createCourses(
            @RequestParam String courseTitles,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            int createdCount = teacherCourseService.createCourses(teacherUserId, courseTitles);
            if (createdCount > 0) {
                redirectAttributes.addFlashAttribute("successMessage", createdCount + "개의 반을 등록했습니다.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "이미 등록된 반만 입력되어 새로 추가된 반이 없습니다.");
            }
            return "redirect:/teacher/courses";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("courseTitles", courseTitles);
            model.addAttribute("courses", teacherCourseService.getCourses(teacherUserId));
            return "teacher/courses";
        }
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}