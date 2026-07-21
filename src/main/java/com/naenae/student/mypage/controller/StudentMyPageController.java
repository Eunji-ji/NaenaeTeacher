package com.naenae.student.mypage.controller;

import com.naenae.common.user.domain.User;
import com.naenae.student.mypage.model.StudentProfileImage;
import com.naenae.student.mypage.service.StudentMyPageService;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentMyPageController {

    private final StudentMyPageService service;

    public StudentMyPageController(StudentMyPageService service) {
        this.service = service;
    }

    @GetMapping("/student/mypage")
    public String page(Authentication authentication, Model model) {
        model.addAttribute("profile", service.get(userId(authentication)));
        return "student/mypage";
    }

    @PostMapping("/student/mypage")
    public String update(
            @RequestParam(required = false) MultipartFile profileImage,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            service.updateProfileImage(userId(authentication), profileImage);
            redirectAttributes.addFlashAttribute("successMessage", "프로필 사진을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/student/mypage";
    }

    @GetMapping("/student/mypage/profile-image")
    public ResponseEntity<Resource> image(Authentication authentication) {
        StudentProfileImage image = service.image(userId(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(image.path()));
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
