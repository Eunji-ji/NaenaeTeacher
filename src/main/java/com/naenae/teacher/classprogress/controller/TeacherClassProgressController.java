package com.naenae.teacher.classprogress.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.classprogress.service.TeacherClassProgressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/classes/progress")
public class TeacherClassProgressController {
    private final TeacherClassProgressService progressService;

    public TeacherClassProgressController(TeacherClassProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public String page(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        populate(model, userId(authentication), null, null, "", page);
        return "teacher/class-progress";
    }

    @PostMapping
    public String create(@RequestParam(required = false) Long courseId,
                         @RequestParam(required = false) Long scheduleId,
                         @RequestParam String memo,
                         Authentication authentication, Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = userId(authentication);
        try {
            progressService.create(userId, courseId, scheduleId, memo);
            redirectAttributes.addFlashAttribute("successMessage", "진도 메모를 등록했습니다.");
            return "redirect:/teacher/classes/progress";
        } catch (IllegalArgumentException exception) {
            populate(model, userId, courseId, scheduleId, memo, 0);
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/class-progress";
        }
    }

    @PostMapping("/{noteId}/delete")
    public String delete(@PathVariable Long noteId, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            progressService.delete(userId(authentication), noteId);
            redirectAttributes.addFlashAttribute("successMessage", "진도 메모를 삭제했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/teacher/classes/progress";
    }

    private void populate(Model model, Long userId, Long courseId, Long scheduleId, String memo, int page) {
        model.addAttribute("courses", progressService.getCourses(userId));
        model.addAttribute("schedules", progressService.getSchedules(userId));
        model.addAttribute("progressPage", progressService.getNotes(userId, page));
        model.addAttribute("progressPaginationUrl", "/teacher/classes/progress");
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedScheduleId", scheduleId);
        model.addAttribute("memo", memo);
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
