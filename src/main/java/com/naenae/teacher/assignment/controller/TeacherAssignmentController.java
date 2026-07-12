package com.naenae.teacher.assignment.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.course.service.TeacherCourseService;
import com.naenae.teacher.assignment.service.TeacherAssignmentService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TeacherAssignmentController {

    private final TeacherCourseService teacherCourseService;
    private final TeacherAssignmentService teacherAssignmentService;

    public TeacherAssignmentController(TeacherCourseService teacherCourseService, TeacherAssignmentService teacherAssignmentService) {
        this.teacherCourseService = teacherCourseService;
        this.teacherAssignmentService = teacherAssignmentService;
    }

    @GetMapping("/teacher/assignments")
    public String assignments(Authentication authentication, Model model) {
        Long teacherId=getTeacherUserId(authentication);
        model.addAttribute("courses", teacherCourseService.getCourses(teacherId));
        model.addAttribute("assignments", teacherAssignmentService.getAssignments(teacherId));
        return "teacher/assignments";
    }

    @PostMapping("/teacher/assignments")
    public String saveAssignment(@RequestParam List<Long> courseIds,
                                 @RequestParam String title,
                                 @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 @RequestParam String descriptionHtml,
                                 @RequestParam(required=false) List<MultipartFile> attachments,
                                 Authentication authentication, RedirectAttributes redirect, Model model) {
        Long teacherId=getTeacherUserId(authentication);
        try {
            teacherAssignmentService.create(teacherId,courseIds,title,startDate,endDate,descriptionHtml,attachments);
            redirect.addFlashAttribute("successMessage","과제를 저장했습니다.");
            return "redirect:/teacher/assignments";
        } catch(IllegalArgumentException|IllegalStateException e) {
            var all=teacherCourseService.getCourses(teacherId);
            model.addAttribute("selectedCourses",all.stream().filter(c->courseIds.contains(c.id())).toList());
            model.addAttribute("errorMessage",e.getMessage()); model.addAttribute("title",title);
            model.addAttribute("startDate",startDate); model.addAttribute("endDate",endDate); model.addAttribute("descriptionHtml",descriptionHtml);
            return "teacher/assignment-form";
        }
    }

    @PostMapping("/teacher/assignments/new")
    public String assignmentForm(
            @RequestParam(required = false) List<Long> courseIds,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        if (courseIds == null || courseIds.isEmpty()) {
            model.addAttribute("errorMessage", "과제를 등록할 반을 1개 이상 선택해 주세요.");
            model.addAttribute("courses", teacherCourseService.getCourses(teacherUserId));
            return "teacher/assignments";
        }
        var selected = teacherCourseService.getCourses(teacherUserId).stream()
                .filter(course -> courseIds.contains(course.id()))
                .toList();
        if (selected.size() != courseIds.stream().distinct().count()) {
            model.addAttribute("errorMessage", "선택한 반 정보를 확인할 수 없습니다.");
            model.addAttribute("courses", teacherCourseService.getCourses(teacherUserId));
            return "teacher/assignments";
        }
        model.addAttribute("selectedCourses", selected);
        model.addAttribute("startDate", LocalDate.now());
        model.addAttribute("endDate", LocalDate.now().plusDays(7));
        return "teacher/assignment-form";
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
