package com.naenae.teacher.auth.controller;

import com.naenae.teacher.auth.service.TeacherSignupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    private final TeacherSignupService teacherSignupService;

    public AuthPageController(TeacherSignupService teacherSignupService) {
        this.teacherSignupService = teacherSignupService;
    }

    @GetMapping("/teacher/login")
    public String teacherLogin() {
        return "auth/login";
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
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            teacherSignupService.signup(name, email, password, passwordConfirm);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/signup";
        }

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/teacher/login";
    }
}
