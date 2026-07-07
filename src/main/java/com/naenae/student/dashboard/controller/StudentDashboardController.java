package com.naenae.student.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentDashboardController {

    @GetMapping("/student")
    public String comingSoon() {
        return "student/coming-soon";
    }
}
