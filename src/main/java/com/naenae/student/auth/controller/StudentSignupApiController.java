package com.naenae.student.auth.controller;

import com.naenae.student.auth.model.StudentSignupCourseOption;
import com.naenae.student.auth.model.StudentSignupStudentOption;
import com.naenae.student.auth.service.StudentSignupService;
import com.naenae.student.auth.security.SignupRateLimitExceededException;
import com.naenae.student.auth.security.StudentSignupRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/student-signup")
public class StudentSignupApiController {

    private final StudentSignupService service;
    private final StudentSignupRateLimiter rateLimiter;

    public StudentSignupApiController(StudentSignupService service, StudentSignupRateLimiter rateLimiter) {
        this.service = service;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/courses")
    public List<StudentSignupCourseOption> courses(@RequestParam String invitationCode, HttpServletRequest request) {
        rateLimiter.checkLookup(request.getRemoteAddr());
        return service.getCourses(invitationCode);
    }

    @GetMapping("/students")
    public List<StudentSignupStudentOption> students(
            @RequestParam String invitationCode,
            @RequestParam Long courseId,
            HttpServletRequest request
    ) {
        rateLimiter.checkLookup(request.getRemoteAddr());
        return service.getStudents(invitationCode, courseId);
    }

    @GetMapping("/login-id-availability")
    public LoginIdAvailability loginIdAvailability(@RequestParam String loginId, HttpServletRequest request) {
        rateLimiter.checkLookup(request.getRemoteAddr());
        boolean available = service.isLoginIdAvailable(loginId);
        return new LoginIdAvailability(available, available ? "사용할 수 있는 아이디입니다." : "이미 사용 중인 아이디입니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(SignupRateLimitExceededException.class)
    public ResponseEntity<ApiError> tooManyRequests(SignupRateLimitExceededException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiError(exception.getMessage()));
    }

    public record LoginIdAvailability(boolean available, String message) {
    }

    public record ApiError(String message) {
    }
}
