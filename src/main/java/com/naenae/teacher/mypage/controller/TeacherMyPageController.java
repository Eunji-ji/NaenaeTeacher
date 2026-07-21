package com.naenae.teacher.mypage.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.mypage.model.ProfileImage;
import com.naenae.teacher.mypage.service.TeacherMyPageService;
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
public class TeacherMyPageController {

    private final TeacherMyPageService service;

    public TeacherMyPageController(TeacherMyPageService service) {
        this.service = service;
    }

    @GetMapping("/teacher/mypage")
    public String page(Authentication authentication, Model model) {
        model.addAttribute("profile", service.get(userId(authentication)));
        return "teacher/mypage";
    }

    @PostMapping("/teacher/mypage")
    public String update(
            @RequestParam String nickname,
            @RequestParam(required = false) MultipartFile profileImage,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            service.update(userId(authentication), nickname, profileImage);
            redirectAttributes.addFlashAttribute("successMessage", "프로필을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/teacher/mypage";
    }

    @PostMapping("/teacher/mypage/invitation-code/reissue")
    public String reissueInvitationCode(Authentication authentication, RedirectAttributes redirectAttributes) {
        service.reissueInvitationCode(userId(authentication));
        redirectAttributes.addFlashAttribute(
                "successMessage", "새 초대코드를 발급했습니다. 이전 코드는 더 이상 사용할 수 없습니다.");
        return "redirect:/teacher/mypage";
    }

    @GetMapping("/teacher/mypage/profile-image")
    public ResponseEntity<Resource> image(Authentication authentication) {
        ProfileImage image = service.image(userId(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(image.path()));
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
