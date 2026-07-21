package com.naenae.student.support;

import com.naenae.common.user.domain.User;
import com.naenae.common.user.repository.UserRepository;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class StudentViewAdvice {

    private final UserRepository userRepository;

    public StudentViewAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("studentDisplayName")
    public String displayName(Authentication authentication) {
        User user = user(authentication);
        return user == null || user.getName() == null || user.getName().isBlank() ? "학생" : user.getName();
    }

    @ModelAttribute("studentInitial")
    public String initial(Authentication authentication) {
        String name = displayName(authentication);
        return name.isBlank() ? "학" : name.substring(0, 1);
    }

    @ModelAttribute("studentHasProfileImage")
    public boolean hasProfileImage(Authentication authentication) {
        User user = user(authentication);
        return user != null && user.getProfileImageStoredName() != null;
    }

    private User user(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            return userRepository.findById(details.getUser().getId()).orElse(details.getUser());
        }
        return null;
    }
}
