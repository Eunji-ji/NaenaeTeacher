package com.naenae.teacher.wordtest.controller;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.wordtest.service.TeacherWordTestService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherWordTestController {

    private final TeacherWordTestService teacherWordTestService;

    public TeacherWordTestController(TeacherWordTestService teacherWordTestService) {
        this.teacherWordTestService = teacherWordTestService;
    }

    @GetMapping("/teacher/word-tests")
    public String wordTests(Authentication authentication, Model model) {
        Long teacherUserId = getTeacherUserId(authentication);
        model.addAttribute("courses", teacherWordTestService.getCourses(teacherUserId));
        model.addAttribute("wordTests", teacherWordTestService.getWordTests(teacherUserId));
        return "teacher/word-tests";
    }

    @PostMapping("/teacher/word-tests/new")
    public String wordTestForm(
            @RequestParam(required = false) List<Long> courseIds,
            Authentication authentication,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            model.addAttribute("selectedCourses", teacherWordTestService.getSelectedCourses(teacherUserId, courseIds));
            model.addAttribute("startDate", LocalDate.now());
            model.addAttribute("endDate", LocalDate.now().plusDays(7));
            model.addAttribute("editMode", false);
            return "teacher/word-test-form";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("courses", teacherWordTestService.getCourses(teacherUserId));
            model.addAttribute("wordTests", teacherWordTestService.getWordTests(teacherUserId));
            return "teacher/word-tests";
        }
    }

    @GetMapping("/teacher/word-tests/{wordTestId}")
    public String wordTestDetail(
            @PathVariable Long wordTestId,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("wordTest", teacherWordTestService.getWordTest(getTeacherUserId(authentication), wordTestId));
        return "teacher/word-test-detail";
    }

    @GetMapping("/teacher/word-tests/{wordTestId}/edit")
    public String editWordTestForm(
            @PathVariable Long wordTestId,
            Authentication authentication,
            Model model
    ) {
        var wordTest = teacherWordTestService.getWordTest(getTeacherUserId(authentication), wordTestId);
        model.addAttribute("selectedCourses", wordTest.courses());
        model.addAttribute("submittedWords", wordTest.words().stream().map(row -> row.word()).toList());
        model.addAttribute("submittedMeanings", wordTest.words().stream().map(row -> row.meaning()).toList());
        model.addAttribute("startDate", wordTest.startDate());
        model.addAttribute("endDate", wordTest.endDate());
        model.addAttribute("wordTestId", wordTest.id());
        model.addAttribute("editMode", true);
        return "teacher/word-test-form";
    }

    @PostMapping("/teacher/word-tests")
    public String createWordTest(
            @RequestParam List<Long> courseIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam List<String> words,
            @RequestParam(required = false) List<String> meanings,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherWordTestService.createWordTest(teacherUserId, courseIds, startDate, endDate, words, meanings);
            redirectAttributes.addFlashAttribute("successMessage", "선택한 반에 단어시험을 등록했습니다.");
            return "redirect:/teacher/word-tests";
        } catch (IllegalArgumentException exception) {
            try {
                model.addAttribute("selectedCourses", teacherWordTestService.getSelectedCourses(teacherUserId, courseIds));
                model.addAttribute("errorMessage", exception.getMessage());
                model.addAttribute("submittedWords", words);
                model.addAttribute("submittedMeanings", meanings);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("editMode", false);
                return "teacher/word-test-form";
            } catch (IllegalArgumentException invalidCourseException) {
                redirectAttributes.addFlashAttribute("errorMessage", invalidCourseException.getMessage());
                return "redirect:/teacher/word-tests";
            }
        }
    }

    @PostMapping("/teacher/word-tests/{wordTestId}")
    public String updateWordTest(
            @PathVariable Long wordTestId,
            @RequestParam List<Long> courseIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam List<String> words,
            @RequestParam(required = false) List<String> meanings,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            teacherWordTestService.updateWordTest(
                    teacherUserId, wordTestId, courseIds, startDate, endDate, words, meanings
            );
            redirectAttributes.addFlashAttribute("successMessage", "단어시험을 수정했습니다.");
            return "redirect:/teacher/word-tests";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("selectedCourses", teacherWordTestService.getSelectedCourses(teacherUserId, courseIds));
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("submittedWords", words);
            model.addAttribute("submittedMeanings", meanings);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("wordTestId", wordTestId);
            model.addAttribute("editMode", true);
            return "teacher/word-test-form";
        }
    }

    @PostMapping("/teacher/word-tests/{wordTestId}/delete")
    public String deleteWordTest(
            @PathVariable Long wordTestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        teacherWordTestService.deleteWordTest(getTeacherUserId(authentication), wordTestId);
        redirectAttributes.addFlashAttribute("successMessage", "단어시험을 삭제했습니다.");
        return "redirect:/teacher/word-tests";
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
