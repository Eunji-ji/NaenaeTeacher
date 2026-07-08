package com.naenae.teacher.student.controller;

import java.nio.charset.StandardCharsets;

import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.student.service.TeacherStudentBulkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/students/bulk")
public class TeacherStudentBulkController {

    private final TeacherStudentBulkService teacherStudentBulkService;

    public TeacherStudentBulkController(TeacherStudentBulkService teacherStudentBulkService) {
        this.teacherStudentBulkService = teacherStudentBulkService;
    }

    @GetMapping
    public String bulkPage(Authentication authentication, Model model) {
        getTeacherUserId(authentication);
        return "teacher/student-bulk";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(Authentication authentication) {
        Long teacherUserId = getTeacherUserId(authentication);
        byte[] template = teacherStudentBulkService.createTemplate(teacherUserId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student-bulk-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Long teacherUserId = getTeacherUserId(authentication);
        try {
            TeacherStudentBulkService.BulkImportResult result = teacherStudentBulkService.importStudents(teacherUserId, file);
            redirectAttributes.addFlashAttribute("successMessage", result.createdCount() + "명의 학생을 등록했습니다.");
            if (!result.errors().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", String.join(" / ", result.errors()));
            }
            return "redirect:/teacher/students/bulk";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "teacher/student-bulk";
        }
    }

    private Long getTeacherUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
