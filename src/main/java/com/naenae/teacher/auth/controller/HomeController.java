package com.naenae.teacher.auth.controller;

import com.naenae.teacher.auth.service.PortfolioDemoLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final PortfolioDemoLoginService portfolioDemoLoginService;

    public HomeController(PortfolioDemoLoginService portfolioDemoLoginService) {
        this.portfolioDemoLoginService = portfolioDemoLoginService;
    }

    @GetMapping("/")
    public String home() {
        return "auth/home";
    }

    @PostMapping("/auth/demo")
    public String portfolioDemo(
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            portfolioDemoLoginService.login(request, response);
            return "redirect:/teacher/dashboard";
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("demoLoginError", exception.getMessage());
            return "redirect:/";
        }
    }
}
