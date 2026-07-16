package com.naenae.teacher.classschedule.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.classschedule.domain.ScheduleWeekday;
import com.naenae.teacher.classschedule.service.TeacherClassScheduleService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/classes/schedule")
public class TeacherClassScheduleController {
    private final TeacherClassScheduleService scheduleService;

    public TeacherClassScheduleController(TeacherClassScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public String page(Authentication authentication, Model model) {
        populate(model, userId(authentication), defaultWeekday(), LocalTime.of(16, 0), LocalTime.of(17, 0), "");
        return "teacher/class-schedule";
    }

    @PostMapping
    public String create(@RequestParam Long courseId, @RequestParam ScheduleWeekday weekday,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                         @RequestParam String lessonTitle, Authentication authentication,
                         Model model, RedirectAttributes redirectAttributes) {
        Long userId = userId(authentication);
        try {
            scheduleService.create(userId, courseId, weekday, startTime, endTime, lessonTitle);
            redirectAttributes.addFlashAttribute("successMessage", "시간표를 등록했습니다.");
            return "redirect:/teacher/classes/schedule";
        } catch (IllegalArgumentException exception) {
            populate(model, userId, weekday, startTime, endTime, lessonTitle);
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/class-schedule";
        }
    }

    @PostMapping("/{scheduleId}/delete")
    public String delete(@PathVariable Long scheduleId, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            scheduleService.delete(userId(authentication), scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "시간표를 삭제했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/teacher/classes/schedule";
    }

    @PostMapping("/all/delete")
    public String deleteAll(Authentication authentication, RedirectAttributes redirectAttributes) {
        long deleted = scheduleService.deleteAll(userId(authentication));
        redirectAttributes.addFlashAttribute("successMessage", deleted + "개의 시간표를 전체 삭제했습니다.");
        return "redirect:/teacher/classes/schedule";
    }

    private void populate(Model model, Long userId, ScheduleWeekday weekday,
                          LocalTime startTime, LocalTime endTime, String lessonTitle) {
        model.addAttribute("courses", scheduleService.getCourses(userId));
        model.addAttribute("weekdays", ScheduleWeekday.values());
        model.addAttribute("selectedWeekday", weekday);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("lessonTitle", lessonTitle);
        model.addAttribute("scheduleDays", scheduleService.getWeeklySchedule(userId));
    }

    private ScheduleWeekday defaultWeekday() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day.getValue() <= 5 ? ScheduleWeekday.valueOf(day.name()) : ScheduleWeekday.MONDAY;
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
