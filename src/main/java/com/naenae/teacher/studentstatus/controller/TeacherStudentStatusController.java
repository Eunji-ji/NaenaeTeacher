package com.naenae.teacher.studentstatus.controller;

import com.naenae.common.user.domain.User;
import com.naenae.student.profile.domain.AcademicExamType;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.studentstatus.service.TeacherStudentStatusService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherStudentStatusController {

    private final TeacherStudentStatusService teacherStudentStatusService;

    public TeacherStudentStatusController(TeacherStudentStatusService teacherStudentStatusService) {
        this.teacherStudentStatusService = teacherStudentStatusService;
    }

    @GetMapping("/teacher/students/status")
    public String status(
            @RequestParam(required = false) Long courseId,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        model.addAttribute("studentLearningPage", teacherStudentStatusService.getPage(teacherUserId, courseId));
        return "teacher/student-learning";
    }

    @GetMapping("/teacher/students/status/{studentId}")
    public String detail(
            @RequestParam(required = false) Long courseId,
            @org.springframework.web.bind.annotation.PathVariable Long studentId,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        model.addAttribute("studentLearningDetailPage", teacherStudentStatusService.getDetailPage(teacherUserId, courseId, studentId));
        model.addAttribute("selectedCourseId", courseId);
        return "teacher/student-learning-detail";
    }

    @PostMapping("/teacher/students/status/{studentId}")
    public String saveMemo(
            @org.springframework.web.bind.annotation.PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String memoSummary,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherStudentStatusService.saveMemo(teacherUserId, studentId, memoSummary);
            redirectAttributes.addFlashAttribute("successMessage", "특징을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToDetail(studentId, courseId);
    }

    @PostMapping("/teacher/students/status/{studentId}/score")
    public String saveScore(
            @org.springframework.web.bind.annotation.PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) AcademicExamType examType,
            @RequestParam(required = false) Integer score,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherStudentStatusService.saveScore(teacherUserId, studentId, examType, score);
            redirectAttributes.addFlashAttribute("successMessage", "성적을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToDetail(studentId, courseId);
    }

    private String redirectToDetail(Long studentId, Long courseId) {
        String redirect = "redirect:/teacher/students/status/" + studentId;
        if (courseId != null) {
            redirect += "?courseId=" + courseId;
        }
        return redirect;
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
