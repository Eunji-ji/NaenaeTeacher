package com.naenae.teacher.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/teacher/login")
    public String teacherLogin() {
        return "auth/login";
    }
}
