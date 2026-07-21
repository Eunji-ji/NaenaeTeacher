package com.naenae.teacher.auth.controller;

import com.naenae.teacher.auth.service.TeacherSignupService;
import com.naenae.student.auth.service.StudentSignupService;
import com.naenae.student.auth.security.SignupRateLimitExceededException;
import com.naenae.student.auth.security.StudentSignupRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    private final TeacherSignupService teacherSignupService;
    private final StudentSignupService studentSignupService;
    private final StudentSignupRateLimiter studentSignupRateLimiter;

    public AuthPageController(TeacherSignupService teacherSignupService, StudentSignupService studentSignupService,
                              StudentSignupRateLimiter studentSignupRateLimiter) {
        this.teacherSignupService = teacherSignupService;
        this.studentSignupService = studentSignupService;
        this.studentSignupRateLimiter = studentSignupRateLimiter;
    }

    @GetMapping("/teacher/login")
    public String teacherLogin() {
        return "auth/login";
    }

    @GetMapping("/student/login")
    public String studentLogin() {
        return "auth/student-login";
    }

    @GetMapping("/student/signup")
    public String studentSignup() {
        return "auth/student-signup";
    }

    @PostMapping("/student/signup")
    public String createStudent(
            @RequestParam String invitationCode,
            @RequestParam Long courseId,
            @RequestParam Long studentId,
            @RequestParam String loginId,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam(defaultValue = "false") boolean termsAgreed,
            @RequestParam(defaultValue = "false") boolean privacyAgreed,
            @RequestParam(defaultValue = "false") boolean ageOrGuardianConfirmed,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            studentSignupRateLimiter.checkSignup(request.getRemoteAddr());
            studentSignupService.signup(invitationCode, courseId, studentId, loginId, password, passwordConfirm,
                    termsAgreed, privacyAgreed, ageOrGuardianConfirmed);
        } catch (IllegalArgumentException | SignupRateLimitExceededException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("invitationCode", invitationCode);
            model.addAttribute("courseId", courseId);
            model.addAttribute("studentId", studentId);
            model.addAttribute("loginId", loginId);
            model.addAttribute("termsAgreed", termsAgreed);
            model.addAttribute("privacyAgreed", privacyAgreed);
            model.addAttribute("ageOrGuardianConfirmed", ageOrGuardianConfirmed);
            return "auth/student-signup";
        }

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/student/login";
    }

    @GetMapping("/teacher/signup")
    public String teacherSignup() {
        return "auth/signup";
    }

    @PostMapping("/teacher/signup")
    public String createTeacher(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam(defaultValue = "false") boolean termsAgreed,
            @RequestParam(defaultValue = "false") boolean privacyAgreed,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            teacherSignupService.signup(name, email, password, passwordConfirm, termsAgreed, privacyAgreed);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("termsAgreed", termsAgreed);
            model.addAttribute("privacyAgreed", privacyAgreed);
            return "auth/signup";
        }

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/teacher/login";
    }
}
