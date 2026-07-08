package com.naenae.teacher.support;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class TeacherViewAdvice {

    @ModelAttribute("teacherName")
    public String teacherName(Authentication authentication) {
        return resolveTeacherName(authentication);
    }

    @ModelAttribute("teacherDisplayName")
    public String teacherDisplayName(Authentication authentication) {
        return resolveTeacherName(authentication) + "쌤";
    }

    @ModelAttribute("teacherInitial")
    public String teacherInitial(Authentication authentication) {
        String teacherName = resolveTeacherName(authentication);
        return teacherName.isBlank() ? "?" : teacherName.substring(0, 1);
    }

    private String resolveTeacherName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            if (user.getName() != null && !user.getName().isBlank()) {
                return user.getName();
            }
        }
        return "선생님";
    }
}
